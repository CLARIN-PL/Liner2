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
 * 
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class CclRelationSaxParser extends DefaultHandler {

    private final String TAG_RELATION 		= "rel";
    private final String TAG_FROM 			= "from";
    private final String TAG_TO 			= "to";
    
    private final String ATTR_REL_SET		= "set";
    private final String ATTR_REL_NAME		= "name";
    private final String ATTR_CHAN_NAME		= "chan";
    private final String ATTR_SENT_ID		= "sent";

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
	
    public CclRelationSaxParser(String uri, InputStream is, Document document) throws DataFormatException {
        this.is = is;
        this.relations = new RelationSet();
        this.document = document;
        parseDocument();
    }
    
    private void parseDocument() throws DataFormatException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(is,this);
        } catch (ParserConfigurationException e) {        	
            throw new DataFormatException("Parse error (ParserConfigurationException)");
        } catch (SAXException e) {
        	e.printStackTrace();
            throw new DataFormatException("Parse error (SAXException)");
        } catch (IOException e) {
            throw new DataFormatException("Parse error (IOException)");
        }
    }

    @Override
    public InputSource resolveEntity (String publicId, String systemId){
        return new InputSource(new StringReader(""));
    }
    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
        if(TAG_RELATION.equalsIgnoreCase(elementName)){
        	currentRelationSet = attributes.getValue(ATTR_REL_SET);
    		currentRelationType = attributes.getValue(ATTR_REL_NAME);
        }
        else if(TAG_FROM.equalsIgnoreCase(elementName)){
        	tmpValue = "";
        	currentFromAnnotationSent = attributes.getValue(ATTR_SENT_ID);
        	currentFromAnnotationChan = attributes.getValue(ATTR_CHAN_NAME);
        	currentFromAnnotationId = 0;
        }
        else if(TAG_TO.equalsIgnoreCase(elementName)){
        	tmpValue = "";
        	currentToAnnotationSent = attributes.getValue(ATTR_SENT_ID);
        	currentToAnnotationChan = attributes.getValue(ATTR_CHAN_NAME);
        	currentToAnnotationId = 0;
        }
    }
    
    
    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        if(TAG_RELATION.equalsIgnoreCase(element)){
        	Annotation annotationFrom = this.document.getAnnotation(currentFromAnnotationSent, currentFromAnnotationChan, currentFromAnnotationId);
        	Annotation annotationTo = this.document.getAnnotation(currentToAnnotationSent, currentToAnnotationChan, currentToAnnotationId);
        	if(annotationFrom != null && annotationTo != null) this.relations.addRelation(new Relation(annotationFrom, annotationTo, currentRelationType, currentRelationType));
        }
        else if(TAG_FROM.equalsIgnoreCase(element)){
        	currentFromAnnotationId = Integer.parseInt(tmpValue);
        }
        else if(TAG_TO.equalsIgnoreCase(element)){
        	currentToAnnotationId = Integer.parseInt(tmpValue);
        }
    }
    
    
    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
        for(int i=start;i<start+length;i++)
            tmpValue += ac[i];
    }


    public Document getDocument(){
    	this.document.setRelations(this.relations);
        return this.document;
    }

}