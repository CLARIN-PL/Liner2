package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TeiCoreferenceSAXParser extends DefaultHandler {

  List<Paragraph> paragraphs;
  TeiDocumentElements elements;
  AnnotationClusterSet coreferenceClusters;
  AnnotationCluster currentRelationCluster;


  public TeiCoreferenceSAXParser(final InputStream is,
                                 final TeiDocumentElements elements) throws DataFormatException {
    paragraphs = elements.getParagraphs();
    this.elements = elements;
    coreferenceClusters = new AnnotationClusterSet();
    parse(is);
  }

  private void parse(final InputStream is) throws DataFormatException {
    try {
      SAXParserFactory.newInstance().newSAXParser().parse(is, this);
    } catch (final ParserConfigurationException e) {
      throw new DataFormatException("Parse error (ParserConfigurationException)");
    } catch (final SAXException e) {
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
    if (elementName.equalsIgnoreCase(Tei.TAG_SEGMENT)) {
      currentRelationCluster = new AnnotationCluster(Relation.COREFERENCE, Relation.COREFERENCE);
    } else if (elementName.equalsIgnoreCase(Tei.TAG_POINTER)) {
      final String target = attributes.getValue("target");
      currentRelationCluster.addAnnotation(elements.getAnnotationMap().get(target));
    }
  }

  @Override
  public void endElement(final String s, final String s1, final String element) throws SAXException {
    if (element.equals(Tei.TAG_SEGMENT)) {
      coreferenceClusters.addRelationCluster(currentRelationCluster);
      currentRelationCluster = null;
    }
  }

  public AnnotationClusterSet getAnnotationClusters() {
    return coreferenceClusters;
  }

}
