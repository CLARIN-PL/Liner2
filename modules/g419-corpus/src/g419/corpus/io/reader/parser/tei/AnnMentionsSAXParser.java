package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnMentionsSAXParser extends DefaultHandler {

    private final String TAG_PARAGRAPH		= "p";
    private final String TAG_SENTENCE		= "s";
    private final String TAG_SEGMENT		= "seg";
    private final String TAG_FEATURESET		= "fs";
    private final String TAG_FEATURE		= "f";
    private final String TAG_STRING	    	= "string";
    private final String TAG_SYMBOL	    	= "symbol";
    private final String TAG_POINTER	   	= "ptr";
    private final String TAG_ID 			= "xml:id";

    public final static String ANNOTATION_MENTION = "anafora_wyznacznik";
    public final static String ANNOTATION_ZERO_MENTION = "wyznacznik_null_verb";
    
    InputStream is;
    ArrayList<Paragraph> paragraphs;
    Paragraph currentParagraph;
    int currentParagraphIdx;
    Sentence currentSentence;
    int currentSentenceIdx;
    Integer currentHead;
    ArrayList<Integer> annotatedTokens;
    String annotationType;
    String currentFeatureName;
    String annotationId;
    HashMap<String,Integer> tokenIdsMap;
    HashMap<String, Annotation> annotationsMap;
    
    public AnnMentionsSAXParser(InputStream is, ArrayList<Paragraph> paragraphs, HashMap<String,Integer> tokenIdsMap) throws DataFormatException {
        this.is = is;
        this.paragraphs = paragraphs;
        this.tokenIdsMap = tokenIdsMap;
        this.annotationsMap = new HashMap<String, Annotation>();
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
        if (elementName.equalsIgnoreCase(TAG_PARAGRAPH)) {
            currentParagraph = paragraphs.get(currentParagraphIdx++);
            currentSentenceIdx = 0;
        }
        else if (elementName.equalsIgnoreCase(TAG_SENTENCE)) {
            currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
        }
        else if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
            annotatedTokens = new ArrayList<Integer>();
            annotationType = null;
            annotationId = attributes.getValue(TAG_ID);
        }
        else if (elementName.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = attributes.getValue("name");
            String currentFeatureValue = attributes.getValue("fVal");
            if("semh".equals(currentFeatureName)){
            	currentHead = tokenIdsMap.get(currentFeatureValue.split("#")[1]);
            }
        }
        else if (elementName.equalsIgnoreCase(TAG_POINTER)) {
            String target = attributes.getValue("target");
            annotatedTokens.add(tokenIdsMap.get(target.split("#")[1]));
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {

        if (element.equals(TAG_SEGMENT)) {
            Annotation ann = new Annotation(annotatedTokens.get(0), ANNOTATION_MENTION, currentSentence);
            for(int i=1; i<annotatedTokens.size(); i++){
                ann.addToken(annotatedTokens.get(i));
            }
            if(currentHead != null){
            	ann.setHead(currentHead);
            	currentHead = null;
            }
            annotationsMap.put(annotationId, ann);
            annotationId = null;
            currentSentence.addChunk(ann);
        }
        else if (element.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = null;
        }

    }

    public ArrayList<Paragraph> getParagraphs(){
        return paragraphs;
    }
    
    public Map<String, Annotation> getMentions(){
    	return annotationsMap;
    }
}
