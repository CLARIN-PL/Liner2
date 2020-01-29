package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Maps;
import g419.corpus.ConsolePrinter;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TeiMorphosyntaxSAXParser extends DefaultHandler {

  final TeiDocumentElements elements;

  Paragraph currentParagraph = null;
  Sentence currentSentence = null;
  Map<String, Tag> currentTokenTags;
  Token currentToken = null;
  String currentFeatureName = "";
  String tmpBase = null;
  String tmpCtag = null;
  String tmpValue;
  String disambTagId;
  int idx = 0;
  boolean foundSentenceId = false;
  TokenAttributeIndex attributeIndex;
  String docName;

  final Logger logger = LoggerFactory.getLogger(getClass());

  public TeiMorphosyntaxSAXParser(final String docName,
                                  final InputStream is,
                                  final TokenAttributeIndex attributeIndex,
                                  final TeiDocumentElements elements)
      throws IOException, ParserConfigurationException, SAXException {
    this.docName = docName;
    this.attributeIndex = attributeIndex;
    this.elements = elements;
    SAXParserFactory.newInstance().newSAXParser().parse(is, this);
    if (!foundSentenceId) {
      ConsolePrinter.log("Generated sentence ids for document:" + docName);
    }
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
    tmpValue = "";
    if (elementName.equalsIgnoreCase(Tei.TAG_PARAGRAPH)) {
      currentParagraph = new Paragraph(attributes.getValue(Tei.TAG_ID));
      currentParagraph.setAttributeIndex(attributeIndex);
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SENTENCE)) {
      currentSentence = new Sentence();
      idx = 0;
      currentSentence.setId(attributes.getValue(Tei.TAG_ID));
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SEGMENT)) {
      currentToken = new Token(attributeIndex);
      currentTokenTags = Maps.newHashMap();
      elements.getTokensIdMap().put("ann_morphosyntax.xml#" + attributes.getValue(Tei.TAG_ID), idx++);
      currentToken.setId(attributes.getValue(Tei.TAG_ID));
      foundSentenceId = true;

    } else if (elementName.equalsIgnoreCase(Tei.TAG_FEATURE)) {
      if (currentFeatureName.equals("disamb") && attributes.getValue("name").equals("choice")) {
        disambTagId = attributes.getValue("fVal").replace("#", "");
      } else {
        currentFeatureName = attributes.getValue("name");
      }
    } else if (elementName.equalsIgnoreCase(Tei.TAG_FEATURESET)) {
      if (currentFeatureName.equals("disamb")) {
        //currentToken.setAttributeValue(attributeIndex.getIndex("tagTool"), attributes.getValue("feats"));
      }
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SYMBOL)) {
      if (currentFeatureName.equals("ctag")) {
        tmpCtag = attributes.getValue("value");
      } else if (currentFeatureName.equals("msd")) {
        final String ctag = tmpCtag + ":" + attributes.getValue("value");
        currentTokenTags.put(attributes.getValue(Tei.TAG_ID), new Tag(tmpBase, ctag, false));
      }
    }
  }

  @Override
  public void endElement(final String s, final String s1, final String element) throws SAXException {
    if (element.equalsIgnoreCase(Tei.TAG_PARAGRAPH)) {
      elements.getParagraphs().add(currentParagraph);
    } else if (element.equalsIgnoreCase(Tei.TAG_SENTENCE)) {
      if (!currentSentence.hasId()) {
        currentSentence.setId("sent" + currentParagraph.numSentences() + 1);
        if (foundSentenceId) {
          logger.warn("Warning: missing sentence id in " + docName + ":" + currentParagraph.getId() + ":" + currentSentence.getId());
        }
      }
      currentParagraph.addSentence(currentSentence);
    } else if (element.equals(Tei.TAG_SEGMENT)) {
      final Tag disambTag = currentTokenTags.get(disambTagId);
      if (disambTag != null) {
        disambTag.setDisamb(true);
      }
      currentTokenTags.values().stream().forEach(currentToken::addTag);
      currentSentence.addToken(currentToken);
    } else if (element.equalsIgnoreCase(Tei.TAG_FEATURE)) {
      currentFeatureName = "";
    } else if (element.equalsIgnoreCase(Tei.TAG_STRING)) {
      if (currentFeatureName.equals("orth")) {
        currentToken.setAttributeValue(attributeIndex.getIndex("orth"), tmpValue);
      } else if (currentFeatureName.equals("base")) {
        tmpBase = tmpValue;
      }
    }
  }

  @Override
  public void characters(final char[] ac, final int start, final int length) throws SAXException {
    for (int i = start; i < start + length; i++) {
      tmpValue += ac[i];
    }
  }

}
