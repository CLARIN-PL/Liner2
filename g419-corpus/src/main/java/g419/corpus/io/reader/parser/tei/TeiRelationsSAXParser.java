package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

public class TeiRelationsSAXParser extends DefaultHandler {

    final private Logger logger = LoggerFactory.getLogger(getClass());
    String relationType;
    String relationSet;
    String relationId;
    String sourceRef = null;
    String targetRef = null;
    Map<String, Annotation> annotationsMap;
    List<Relation> relations = Lists.newArrayList();

    public TeiRelationsSAXParser(final InputStream is, final Map<String, Annotation> annotationsMap) throws DataFormatException {
        this.annotationsMap = annotationsMap;
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser parser = factory.newSAXParser();
            parser.parse(is, this);
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
        switch (elementName.toLowerCase()) {
            case Tei.TAG_SEGMENT:
                relationType = attributes.getValue("type");
                relationSet = attributes.getValue("set");
                relationId = attributes.getValue(Tei.TAG_ID);
                break;

            case Tei.TAG_POINTER:
                switch (attributes.getValue("type")) {
                    case "source":
                        sourceRef = attributes.getValue("target");
                        break;
                    case "target":
                        targetRef = attributes.getValue("target");
                        break;
                }
                break;
        }
    }

    @Override
    public void endElement(final String s, final String s1, final String element) throws SAXException {
        if (element.equals(Tei.TAG_SEGMENT)) {
            final Annotation sourceAnn = annotationsMap.get(sourceRef);
            final Annotation targetAnn = annotationsMap.get(targetRef);
            if (sourceAnn == null) {
                logger.error("Relation was skipped because source annotation was not found for the id {}", sourceRef);
            } else if (targetAnn == null) {
                logger.error("Relation was skipped because target annotation was not found for the id {}", targetRef);
            } else {

                relations.add(new Relation(relationId, sourceAnn, targetAnn, relationType, relationSet));
            }
            relationType = null;
            sourceRef = null;
            targetRef = null;
        }

    }

    public List<Relation> getRelations() {
        return relations;
    }
}
