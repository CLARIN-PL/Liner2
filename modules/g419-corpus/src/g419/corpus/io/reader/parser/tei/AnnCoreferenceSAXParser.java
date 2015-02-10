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
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnCoreferenceSAXParser extends DefaultHandler {
		
		private final String TAG_PARAGRAPH		= "p";
	    private final String TAG_SENTENCE		= "s";
	    private final String TAG_SEGMENT		= "seg";
	    private final String TAG_FEATURESET		= "fs";
	    private final String TAG_FEATURE		= "f";
	    private final String TAG_STRING	    	= "string";
	    private final String TAG_SYMBOL	    	= "symbol";
	    private final String TAG_POINTER	   	= "ptr";
	    private final String TAG_ID 			= "xml:id";

	    InputStream is;
	    ArrayList<Paragraph> paragraphs;
	    Paragraph currentParagraph;
	    int currentParagraphIdx;
	    Sentence currentSentence;
	    int currentSentenceIdx;
	    ArrayList<Integer> annotatedTokens;
	    String annotationType;
	    String currentFeatureName;
	    Map<String, Annotation> annotationsMap;
	    AnnotationClusterSet coreferenceClusters;
	    AnnotationCluster currentRelationCluster;


	    public AnnCoreferenceSAXParser(InputStream is, ArrayList<Paragraph> paragraphs, Map<String, Annotation> annotationsMap) throws DataFormatException {
	        this.is = is;
	        this.paragraphs = paragraphs;
	        this.annotationsMap = annotationsMap;
	        this.coreferenceClusters = new AnnotationClusterSet();
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
	            currentRelationCluster = new AnnotationCluster(Relation.COREFERENCE);
	        }
	        else if (elementName.equalsIgnoreCase(TAG_POINTER)) {
	            String target = attributes.getValue("target").split("#")[1];
	            currentRelationCluster.addAnnotation(annotationsMap.get(target));
	        }
	    }

	    @Override
	    public void endElement(String s, String s1, String element) throws SAXException {

	        if (element.equals(TAG_SEGMENT)) {
	        	this.coreferenceClusters.addRelationCluster(currentRelationCluster);
	        	currentRelationCluster = null;
	        }

	    }

	    public RelationSet getRelations(){
	    	return this.coreferenceClusters.getRelationSet(null);
	    }
	    
	    
}
