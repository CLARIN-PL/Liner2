package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.schema.tei.TeiAnnProps;
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
import java.util.HashMap;
import java.util.Map;

public class TeiPropsSAXParser extends DefaultHandler {

    private StringBuilder text = new StringBuilder();
    private Map<String, String> currentProps = new HashMap<>();
    private String currentName = null;

    Map<String, Map<String, String>> props = new HashMap<>();

    public TeiPropsSAXParser(final InputStream is) throws DataFormatException, ParserConfigurationException, SAXException, IOException {
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
        if (text.length() > 0) {
            text = new StringBuilder();
        }
        if (elementName.equalsIgnoreCase(TeiAnnProps.TAG_SEGMENT)) {
            String id = attributes.getValue(TeiAnnProps.ATTR_CORESP);
            if (id != null) {
                id = id.split("#")[1];
            }
            currentProps = new HashMap<>();
            props.put(id, currentProps);
        } else if (elementName.equalsIgnoreCase(TeiAnnProps.TAG_FEATURE)) {
            currentName = attributes.getValue(TeiAnnProps.ATTR_NAME);
        }
    }

    @Override
    public void endElement(final String s, final String s1, final String element) throws SAXException {
        if (element.equalsIgnoreCase(TeiAnnProps.TAG_FEATURE)) {
            final String value = text.toString();
            currentProps.put(currentName, value);
        }
    }

    @Override
    public void characters(final char[] ac, final int start, final int length) throws SAXException {
        text.append(ac, start, length);
    }

    public Map<String, Map<String, String>> getProps() {
        return props;
    }

}
