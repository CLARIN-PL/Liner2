package g419.corpus.io.reader.parser.tei;

import g419.corpus.schema.tei.TeiMetadata;
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

public class TeiMetadataSAXParser extends DefaultHandler {

    private final Map<String, String> metadata = new HashMap<>();

    public TeiMetadataSAXParser(final InputStream is) throws ParserConfigurationException, SAXException, IOException {
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
        if (elementName.equalsIgnoreCase(TeiMetadata.TAG_METADATA)) {
            final String name = attributes.getValue(TeiMetadata.ATTR_METADATA_NAME);
            final String value = attributes.getValue(TeiMetadata.ATTR_METADATA_VALUE);
            metadata.put(name, value);
        }
    }

    @Override
    public void endElement(final String s, final String s1, final String element) throws SAXException {
    }

    @Override
    public void characters(final char[] ac, final int start, final int length) throws SAXException {
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

}
