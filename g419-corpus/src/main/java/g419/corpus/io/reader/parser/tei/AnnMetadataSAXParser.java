package g419.corpus.io.reader.parser.tei;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import g419.corpus.schema.tei.TeiMetadata;

public class AnnMetadataSAXParser extends DefaultHandler{
    
    private Map<String, String> metadata = new HashMap<String, String>();

    public AnnMetadataSAXParser(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(is,this);
    }

    @Override
    public InputSource resolveEntity (String publicId, String systemId){
        return new InputSource(new StringReader(""));
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
        if (elementName.equalsIgnoreCase(TeiMetadata.TAG_METADATA)) {
        	String name = attributes.getValue(TeiMetadata.ATTR_METADATA_NAME);
        	String value = attributes.getValue(TeiMetadata.ATTR_METADATA_VALUE);
        	this.metadata.put(name, value);
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
    }

    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
    }
    
    public Map<String, String> getMetadata(){
    	return this.metadata;
    }

}
