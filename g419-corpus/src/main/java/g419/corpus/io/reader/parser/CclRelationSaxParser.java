package g419.corpus.io.reader.parser;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Adam Kaczmarek
 */
public class CclRelationSaxParser extends DefaultHandler {

  private final String TAG_RELATION = "rel";
  private final String TAG_FROM = "from";
  private final String TAG_TO = "to";

  private final String ATTR_REL_SET = "set";
  private final String ATTR_REL_NAME = "name";
  private final String ATTR_CHAN_NAME = "chan";
  private final String ATTR_SENT_ID = "sent";

  InputStream is;
  String tmpValue;

  Document document = null;
  RelationSet relations;
  String currentRelationType;
  String currentRelationSet;

  String currentFromAnnotationSent;
  String currentFromAnnotationChan;
  int currentFromAnnotationId;

  String currentToAnnotationSent;
  String currentToAnnotationChan;
  int currentToAnnotationId;

  public CclRelationSaxParser(final String uri, final InputStream is, final Document document) throws DataFormatException {
    this.is = is;
    relations = new RelationSet();
    this.document = document;
    parseDocument();
  }

  private void parseDocument() throws DataFormatException {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      final SAXParser parser = factory.newSAXParser();
      parser.parse(is, this);
    } catch (final ParserConfigurationException e) {
      throw new DataFormatException("Parse error (ParserConfigurationException)");
    } catch (final SAXException e) {
      e.printStackTrace();
      throw new DataFormatException("Parse error (SAXException)");
    } catch (final IOException e) {
      throw new DataFormatException("Parse error (IOException)");
    }
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
    if (TAG_RELATION.equalsIgnoreCase(elementName)) {
      currentRelationSet = attributes.getValue(ATTR_REL_SET);
      currentRelationType = attributes.getValue(ATTR_REL_NAME);
    } else if (TAG_FROM.equalsIgnoreCase(elementName)) {
      tmpValue = "";
      currentFromAnnotationSent = attributes.getValue(ATTR_SENT_ID);
      currentFromAnnotationChan = attributes.getValue(ATTR_CHAN_NAME);
      currentFromAnnotationId = 0;
    } else if (TAG_TO.equalsIgnoreCase(elementName)) {
      tmpValue = "";
      currentToAnnotationSent = attributes.getValue(ATTR_SENT_ID);
      currentToAnnotationChan = attributes.getValue(ATTR_CHAN_NAME);
      currentToAnnotationId = 0;
    }
  }


  @Override
  public void endElement(final String s, final String s1, final String element) throws SAXException {
    if (TAG_RELATION.equalsIgnoreCase(element)) {
      final Annotation annotationFrom =
          document.getAnnotation(currentFromAnnotationSent, currentFromAnnotationChan, currentFromAnnotationId);
      final Annotation annotationTo =
          document.getAnnotation(currentToAnnotationSent, currentToAnnotationChan, currentToAnnotationId);
      if (annotationFrom != null && annotationTo != null) {
        relations.addRelation(
            new Relation(annotationFrom, annotationTo, currentRelationType, currentRelationSet, document));
      }
    } else if (TAG_FROM.equalsIgnoreCase(element)) {
      currentFromAnnotationId = Integer.parseInt(tmpValue);
    } else if (TAG_TO.equalsIgnoreCase(element)) {
      currentToAnnotationId = Integer.parseInt(tmpValue);
    }
  }


  @Override
  public void characters(final char[] ac, final int start, final int length) throws SAXException {
    for (int i = start; i < start + length; i++) {
      tmpValue += ac[i];
    }
  }


  public Document getDocument() {
    document.setRelations(relations);
    return document;
  }

}