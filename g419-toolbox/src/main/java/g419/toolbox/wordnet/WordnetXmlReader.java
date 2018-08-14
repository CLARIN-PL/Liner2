package g419.toolbox.wordnet;

import g419.toolbox.wordnet.struct.LexicalRelation;
import g419.toolbox.wordnet.struct.LexicalUnit;
import g419.toolbox.wordnet.struct.WordnetPl;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Klasa do wczytania wordnetu z pliku XML.
 * @author czuk
 *
 */
public class WordnetXmlReader extends DefaultHandler {

    private final String TAG_LEXICAL_UNIT	= "lexical-unit";
    private final String TAG_LEXICAL_RELATION = "lexicalrelations";
    
    private final String ATTR_LEXICAL_RELATION_PARENT = "parent";
    private final String ATTR_LEXICAL_RELATION_CHILD = "child";
    private final String ATTR_LEXICAL_RELATION_RELATION = "relation";
    
    private final String ATTR_LEXICAL_UNIT_ID = "id";
    private final String ATTR_LEXICAL_UNIT_NAME = "name";
    private final String ATTR_LEXICAL_UNIT_POS = "pos";
    private final String ATTR_LEXICAL_UNIT_DOMAIN = "domain";
    private final String ATTR_LEXICAL_UNIT_DESC = "desc";
    private final String ATTR_LEXICAL_UNIT_WORKSTATE = "workstate";
    private final String ATTR_LEXICAL_UNIT_SOURCE = "source";
    private final String ATTR_LEXICAL_UNIT_VARIANT = "variant";

    StringBuilder tmpValue = new StringBuilder();
    WordnetPl wordnet = new WordnetPl();
 	
    public static WordnetPl load(String filename){
    	WordnetPl wordnet = new WordnetPl();
    	InputStream is = null;
    	try{
    		is = new FileInputStream(filename);
    		SAXParserFactory factory = SAXParserFactory.newInstance();
    		SAXParser parser = factory.newSAXParser();
    		parser.parse(is,new WordnetXmlReader(wordnet));
    	}
    	catch(Exception ex){
    		
    	}
    	finally{
    		if ( is != null ){
    			try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    	return wordnet;
    }
    
    public WordnetXmlReader(WordnetPl wordnet) {
    	this.wordnet = wordnet;
    }
    
    @Override
    public InputSource resolveEntity (String publicId, String systemId){
        return new InputSource(new StringReader(""));
    }
    
    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
    	if ( this.tmpValue.length() > 0 ){
    		tmpValue = new StringBuilder();
    	}
    
        if(TAG_LEXICAL_UNIT.equalsIgnoreCase(elementName)){
        	String id = attributes.getValue(ATTR_LEXICAL_UNIT_ID);
        	String name = attributes.getValue(ATTR_LEXICAL_UNIT_NAME);
        	LexicalUnit unit = new LexicalUnit(id, name);
        	this.wordnet.addLexicalUnit(unit);
        }
        else if(TAG_LEXICAL_RELATION.equalsIgnoreCase(elementName)){
        	String parent = attributes.getValue(ATTR_LEXICAL_RELATION_PARENT);
        	String child = attributes.getValue(ATTR_LEXICAL_RELATION_CHILD);
        	String relation = attributes.getValue(ATTR_LEXICAL_RELATION_RELATION);
        	
        	LexicalUnit parentUnit = this.wordnet.getLexicalUnit(parent);
        	LexicalUnit childUnit = this.wordnet.getLexicalUnit(child);
        	
        	if ( parentUnit != null && childUnit != null ){
        		LexicalRelation r = new LexicalRelation(parentUnit, childUnit, relation);
        		this.wordnet.addLexicalRelation(r);
        	}
        	else {
        		Logger.getLogger(this.getClass()).error("Parent or child lexical unit not found");
        	}
        }
    }
    
    
    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
    }
    
    
    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
        for(int i=start;i<start+length;i++)
            this.tmpValue.append(ac, start, length);
    }


}
