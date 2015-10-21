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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/28/13
 * Time: 8:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnNamedSAXParser extends DefaultHandler {

    private final String TAG_PARAGRAPH		= "p";
    private final String TAG_SENTENCE		= "s";
    private final String TAG_SEGMENT		= "seg";
    private final String TAG_FEATURESET	= "fs";
    private final String TAG_FEATURE		= "f";
    private final String TAG_STRING	    = "string";
    private final String TAG_SYMBOL	    = "symbol";
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
    HashMap<String,Integer> tokenIdsMap;


    public AnnNamedSAXParser(InputStream is, ArrayList<Paragraph> paragraphs, HashMap<String,Integer> tokenIdsMap) throws DataFormatException {
        this.is = is;
        this.paragraphs = paragraphs;
        this.tokenIdsMap = tokenIdsMap;
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
            throw new DataFormatException("Parse error (SAXException): " + e.getMessage());
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
            currentParagraph =paragraphs.get(currentParagraphIdx++);
            currentSentenceIdx = 0;
        }
        else if (elementName.equalsIgnoreCase(TAG_SENTENCE)) {
            currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
        }
        else if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
            annotatedTokens = new ArrayList<Integer>();
            annotationType = null;
        }
        else if (elementName.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = attributes.getValue("name");
        }
        else if (elementName.equalsIgnoreCase(TAG_SYMBOL)) {
            if (currentFeatureName.equals("type")){
                annotationType =  attributes.getValue("value");
            }
            else if (currentFeatureName.equals("subtype")){
                annotationType +=  "-" + attributes.getValue("value");
            }
        }
        else if (elementName.equalsIgnoreCase(TAG_POINTER)) {
            String target = attributes.getValue("target");
            Integer tokenIndex = tokenIdsMap.get(target.split("#")[1]);
            if ( tokenIndex == null ){
            	throw new SAXException("Token with id '" + target + "' not found");
            }
            annotatedTokens.add(tokenIndex);
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {

        if (element.equals(TAG_SEGMENT)) {
            Annotation ann = new Annotation(annotatedTokens.get(0), annotationType, currentSentence);
            for(int i=1; i<annotatedTokens.size(); i++){
                ann.addToken(annotatedTokens.get(i));
            }
            currentSentence.addChunk(ann);
        }
        else if (element.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = null;
        }

    }

    public ArrayList<Paragraph> getParagraphs(){
        return paragraphs;
    }

}
