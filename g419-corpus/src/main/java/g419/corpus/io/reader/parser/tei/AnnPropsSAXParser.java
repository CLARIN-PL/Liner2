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

public class AnnPropsSAXParser extends DefaultHandler{
    
    private StringBuilder text = new StringBuilder();
    private Map<String, String> currentProps = new HashMap<String, String>();
    private String currentName = null;

    Map<String, Map<String, String>> props = new HashMap<String, Map<String, String>>();

    public AnnPropsSAXParser(InputStream is) throws DataFormatException, ParserConfigurationException, SAXException, IOException {
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
    	if ( this.text.length() > 0 ){
    		this.text = new StringBuilder();
    	}
        if (elementName.equalsIgnoreCase(TeiAnnProps.TAG_SEGMENT)) {
        	String id = attributes.getValue(TeiAnnProps.ATTR_CORESP);
        	if ( id != null ){
        		id = id.split("#")[1];
        	}
        	this.currentProps = new HashMap<String, String>();
        	this.props.put(id, this.currentProps);
        }
        else if ( elementName.equalsIgnoreCase(TeiAnnProps.TAG_FEATURE) ){
        	this.currentName = attributes.getValue(TeiAnnProps.ATTR_NAME);
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        if (element.equalsIgnoreCase(TeiAnnProps.TAG_FEATURE)) {
            String value = this.text.toString();
            this.currentProps.put(this.currentName, value);
        }
    }

    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
        this.text.append(ac, start, length);
    }
    
    public Map<String, Map<String, String>> getProps(){
    	return this.props;
    }

}
