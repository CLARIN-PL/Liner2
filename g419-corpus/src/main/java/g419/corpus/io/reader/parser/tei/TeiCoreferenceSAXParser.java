package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

public class TeiCoreferenceSAXParser extends DefaultHandler {

    ArrayList<Paragraph> paragraphs;
    Map<String, Annotation> annotationsMap;
    AnnotationClusterSet coreferenceClusters;
    AnnotationCluster currentRelationCluster;


    public TeiCoreferenceSAXParser(final InputStream is, final ArrayList<Paragraph> paragraphs, final Map<String, Annotation> annotationsMap) throws DataFormatException {
        this.paragraphs = paragraphs;
        this.annotationsMap = annotationsMap;
        coreferenceClusters = new AnnotationClusterSet();
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
            final String target = attributes.getValue("target").split("#")[1];
            currentRelationCluster.addAnnotation(annotationsMap.get(target));
        }
    }

    @Override
    public void endElement(final String s, final String s1, final String element) throws SAXException {
        if (element.equals(Tei.TAG_SEGMENT)) {
            coreferenceClusters.addRelationCluster(currentRelationCluster);
            currentRelationCluster = null;
        }
    }

    public RelationSet getRelations() {
        return coreferenceClusters.getRelationSet(null);
    }


}
