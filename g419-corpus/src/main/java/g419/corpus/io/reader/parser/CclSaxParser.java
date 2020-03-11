package g419.corpus.io.reader.parser;

import g419.corpus.ConsolePrinter;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/7/13
 * Time: 9:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class CclSaxParser extends DefaultHandler {

  private final String TAG_ANN = "ann";
  private final String TAG_BASE = "base";
  private final String TAG_CHAN = "chan";
  private final String TAG_CTAG = "ctag";
  private final String TAG_DISAMB = "disamb";
  private final String TAG_ID = "id";
  private final String TAG_ORTH = "orth";
  private final String TAG_NS = "ns";
  private final String TAG_PARAGRAPH = "chunk";
  private final String TAG_PARAGRAPH_SET = "chunkSet";
  private final String TAG_SENTENCE = "sentence";
  private final String TAG_TAG = "lex";
  private final String TAG_TOKEN = "tok";
  private final String TAG_HEAD = "head";
  private final String TAG_PROP = "prop";
  private final String ATTR_KEY = "key";

  protected ArrayList<Paragraph> paragraphs = new ArrayList<>();
  private Paragraph currentParagraph = null;
  Sentence currentSentence = null;
  HashMap<String, String> chunkMetaData;
  Hashtable<String, Annotation> annotations;
  Token currentToken = null;
  String tmpBase = null;
  String tmpCtag = null;
  InputStream is;
  String tmpValue;
  Boolean tmpDisamb = false;
  int idx = 0;
  String chanName;
  String chanHead;
  boolean foundDisamb;
  TokenAttributeIndex attributeIndex;
  Document document = null;
  boolean foundSentenceId = false;
  String uri;
  Map<String, String> tmpProps;
  Map<String, Annotation> annotationsPerToken;
  String propKey;

  public CclSaxParser(final String uri, final InputStream is, final TokenAttributeIndex attributeIndex) throws DataFormatException, ParserConfigurationException, SAXException, IOException {
    this.uri = uri;
    this.is = is;
    this.attributeIndex = attributeIndex;
    parseDocument();
    document = new Document(uri, paragraphs, this.attributeIndex);
  }

  private void parseDocument() throws DataFormatException, ParserConfigurationException, SAXException, IOException {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser parser = factory.newSAXParser();
    parser.parse(is, this);
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
    tmpValue = "";
    if (elementName.equalsIgnoreCase(TAG_PARAGRAPH)) {
      chunkMetaData = new HashMap<>();
      for (int i = 0; i < attributes.getLength(); i++) {
        if (!attributes.getQName(i).equals(TAG_ID)) {
          if (!attributes.getQName(i).contains(":href")) {
            chunkMetaData.put(attributes.getQName(i), attributes.getValue(i));
          } else {
            chunkMetaData.put("xlink:href", attributes.getValue(i));
          }
        }
      }
      currentParagraph = new Paragraph(attributes.getValue(TAG_ID));
      currentParagraph.setChunkMetaData(chunkMetaData);
      currentParagraph.setAttributeIndex(attributeIndex);
    } else if (elementName.equalsIgnoreCase(TAG_SENTENCE)) {
      currentSentence = new Sentence();
      currentSentence.setParagraph(currentParagraph);
      currentSentence.setDocument(document);
      annotations = new Hashtable<>();
      idx = 0;
      currentSentence.setId(attributes.getValue(TAG_ID));
    } else if (elementName.equalsIgnoreCase(TAG_TOKEN)) {
      currentToken = new Token(attributeIndex);
      currentToken.setId(attributes.getValue(TAG_ID));
      tmpProps = new HashMap<>();
      annotationsPerToken = new HashMap<>();
      currentSentence.addToken(currentToken);
    } else if (elementName.equalsIgnoreCase(TAG_TAG)) {
      if (attributes.getValue(TAG_DISAMB) == null) {
        tmpDisamb = false;
      } else if (Integer.parseInt(attributes.getValue(TAG_DISAMB)) == 0) {
        tmpDisamb = false;
      } else {
        tmpDisamb = true;
      }
    } else if (elementName.equalsIgnoreCase(TAG_ANN)) {
      chanName = attributes.getValue(TAG_CHAN);
      chanHead = attributes.getValue(TAG_HEAD) != null ?
          attributes.getValue(TAG_HEAD) : "0";
    } else if (elementName.equalsIgnoreCase(TAG_NS)) {
      if (currentToken != null) {
        currentToken.setNoSpaceAfter(true);
      }
    } else if (elementName.equalsIgnoreCase(TAG_PROP)) {
      propKey = attributes.getValue(ATTR_KEY);
    }
  }

  @Override
  public void endElement(final String s, final String s1, final String element) throws SAXException {
    if (element.equals(TAG_PARAGRAPH)) {
      paragraphs.add(currentParagraph);
      onParagraphRead();
    } else if (element.equalsIgnoreCase(TAG_SENTENCE)) {
      for (final Annotation chunk : annotations.values()) {
        chunk.assignHead();
        currentSentence.addChunk(chunk);
      }
      if (!currentSentence.hasId()) {
        currentSentence.setId("sent" + (currentParagraph.numSentences() + 1));
        if (foundSentenceId) {
          System.out.println("Warning: missing sentence id in " + uri + ":" + currentParagraph.getId() + ":" + currentSentence.getId());
        }
      } else {
        foundSentenceId = true;
      }
      currentParagraph.addSentence(currentSentence);
    } else if (element.equalsIgnoreCase(TAG_TOKEN)) {
      final ArrayList<Tag> tags = currentToken.getTags();
      foundDisamb = false;
      for (final Tag tag : tags) {
        if (tag.getDisamb()) {
          currentToken.setAttributeValue(attributeIndex.getIndex("base"), tag.getBase());
          currentToken.setAttributeValue(attributeIndex.getIndex("ctag"), tag.getCtag());
          foundDisamb = true;
          break;
        }
      }
      if (!foundDisamb && tags.size() > 0) {
        currentToken.setAttributeValue(attributeIndex.getIndex("base"), tags.get(0).getBase());
        currentToken.setAttributeValue(attributeIndex.getIndex("ctag"), tags.get(0).getCtag());
      }
      idx++;
      for (final String propertyKey : tmpProps.keySet()) {
        // todo: assert parts.length==2
        final String[] parts = propertyKey.split("[:]");
        final String channel = parts[0];
        if (annotationsPerToken.keySet().contains(channel)) {
          annotationsPerToken.get(channel).setMetadata(parts[1], tmpProps.get(propertyKey));
        } else {
          String sentId = currentSentence.getId();
          if (sentId == null) {
            sentId = "unknown_sentence_id";
          }
          currentToken.setProp(propertyKey, tmpProps.get(propertyKey));
        }
      }
    } else if (element.equalsIgnoreCase(TAG_ORTH)) {
      currentToken.setAttributeValue(attributeIndex.getIndex("orth"), tmpValue);
    } else if (element.equalsIgnoreCase(TAG_TAG)) {
      currentToken.addTag(new Tag(tmpBase, tmpCtag, tmpDisamb));
    } else if (element.equalsIgnoreCase(TAG_BASE)) {
      tmpBase = tmpValue;
    } else if (element.equalsIgnoreCase(TAG_CTAG)) {
      tmpCtag = tmpValue;
    } else if (element.equalsIgnoreCase(TAG_ANN)) {
      final String chanNumber = tmpValue.trim();
      Annotation annotation = null;
      if (!chanNumber.equals("0")) {
        final AnnChan ann = new AnnChan(chanName, chanNumber, chanHead);
        if (annotations.containsKey(ann.toString())) {
          annotation = annotations.get(ann.toString());
          annotation.addToken(idx);
        } else {
          annotation = new Annotation(idx, ann.chan, Integer.parseInt(chanNumber), currentSentence);
          annotation.setId(chanNumber);
          annotations.put(ann.toString(), annotation);
        }
        if ("1".equals(ann.head)) {
          annotations.get(ann.toString()).setHead(idx);
        }
        annotationsPerToken.put(chanName, annotation);
      }

    } else if (element.equalsIgnoreCase(TAG_PROP)) {
      tmpProps.put(propKey, tmpValue);
    }
  }

  @Override
  public void characters(final char[] ac, final int start, final int length) throws SAXException {
    for (int i = start; i < start + length; i++) {
      tmpValue += ac[i];
    }
  }


  public Document getDocument() {
    if (!foundSentenceId) {
      ConsolePrinter.log("Generated sentence ids for document:" + uri);
    }
    return document;
  }

  /**
   * Funkcja wywoływana jest po wczytaniu paragrafu (chunku).
   */
  public void onParagraphRead() {

  }

  class AnnChan {

    public String chan = null;
    public String number = null;
    public String head = "0";

    public AnnChan(final String chan, final String number, final String head) {
      this.chan = chan;
      this.number = number;
      this.head = head;
    }

    @Override
    public String toString() {
      return chan + "#" + number;
    }
  }
}
