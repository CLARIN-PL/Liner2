package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationSet;
import g419.corpus.structure.Sentence;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnRelationsSAXParser extends DefaultHandler {
		
		private final String TAG_PARAGRAPH		= "p";
	    private final String TAG_SENTENCE		= "s";
	    private final String TAG_SEGMENT		= "seg";
	    private final String TAG_FEATURESET		= "fs";
	    private final String TAG_FEATURE		= "f";
	    private final String TAG_STRING	    	= "string";
	    private final String TAG_SYMBOL	    	= "symbol";
	    private final String TAG_POINTER	   	= "ptr";
	    private final String TAG_ID 			= "xml:id";

	    InputStream is = null;
	    String relationType;
	    String sourceRef = null;
	    String targetRef = null;
	    Map<String, Annotation> annotationsMap = null;
	    
	    List<Relation> relations = new LinkedList<Relation>();


	    public AnnRelationsSAXParser(InputStream is, Map<String, Annotation> annotationsMap) throws DataFormatException {
	        this.is = is;
	        this.annotationsMap = annotationsMap;
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
	        if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
	        	this.relationType = attributes.getValue("type");
	        }
	        else if (elementName.equalsIgnoreCase(TAG_POINTER)) {
	        	if ( attributes.getValue("type").equals("source") ){
	        		this.sourceRef = attributes.getValue("target");
	        	}
	        	else if ( attributes.getValue("type").equals("target") ){
	        		this.targetRef = attributes.getValue("target");
	        	}
	        }
	    }

	    @Override
	    public void endElement(String s, String s1, String element) throws SAXException {

	        if (element.equals(TAG_SEGMENT)) {
	        	// TODO: sprawdzić, czy zostały wczytane anotacje dla sourceRef i targetRef
	        	Annotation sourceAnn = this.annotationsMap.get(this.sourceRef);
	        	Annotation targetAnn = this.annotationsMap.get(this.targetRef);
	        	if ( sourceAnn != null && targetAnn != null ){
		        	Relation r = new Relation(sourceAnn, targetAnn, this.relationType);
		        	this.relations.add(r);	        		
	        	}
	        	else{
	        		// TODO: zamienić na logger
	        		System.err.println("TEI error: relacja została pominięta");
	        		if ( sourceAnn == null ){
	        			System.err.println("Anotacja dla " + sourceRef + " nie została znaleziona");
	        		}
	        		if ( targetAnn == null ){
	        			System.err.println("Anotacja dla " + targetRef + " nie została znaleziona");
	        		}
	        	}
	        	this.relationType = null;
	        	this.sourceRef = null;
	        	this.targetRef = null;
	        }

	    }

	    public List<Relation> getRelations(){
	    	return this.relations;
	    }
	    
	    
}
