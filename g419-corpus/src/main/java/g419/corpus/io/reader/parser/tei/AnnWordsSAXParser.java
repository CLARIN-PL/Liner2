package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.tei.AnnGroupsSAXParser.SentenceGroup;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 */
public class AnnWordsSAXParser extends DefaultHandler{

    private final String TAG_PARAGRAPH		= "p";
    private final String TAG_SENTENCE		= "s";
    private final String TAG_SEGMENT		= "seg";
    private final String TAG_PTR	    	= "ptr";
    private final String TAG_ID 			= "xml:id";
    private final String TAG_FEATURE		= "f";
    private final String TAG_SYMBOL	    = "symbol";

    HashMap<String,List<String>> wordsIdsMap = new HashMap<String, List<String>>();
    InputStream is = null;
    String docName = null;
    ArrayList<Paragraph> paragraphs = null;
    Map<String, Integer> tokenIdsMap = null;
    
    /** A list of tokens for the current word */
    List<String> currentWord = null;
    int currentParagraphIdx = 0;
    int currentSentenceIdx = 0;
    Paragraph currentParagraph = null;
    Sentence currentSentence = null;
    String currentWordId = null;
    String currentAnnotationType = null;
    String currentFeatureName = null;
    List<String> currentWordTokens = null;

    /** Map of group names and their words and groups. */
    Map<String, List<String>> wordsTokensId = new HashMap<String, List<String>>();

    /** Collection of recognized <word> elements. */
    List<SentenceWord> words = new LinkedList<SentenceWord>();

    /**
     * 
     * @param docName
     * @param is
     * @throws DataFormatException
     */
    public AnnWordsSAXParser(String docName, InputStream is, ArrayList<Paragraph> paragraphs, Map<String,Integer> tokenIdsMap) throws DataFormatException {
        this.docName = docName;
        this.is = is;
        this.paragraphs = paragraphs;
        this.tokenIdsMap = tokenIdsMap;
        
        this.parseDocument();
    }

    public HashMap<String,List<String>> getWordsIdsMap(){
        return wordsIdsMap;
    }

    private void parseDocument() throws DataFormatException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(is,this);
            this.resolvePtrToWords();
            this.makeAnnotations();
        } catch (ParserConfigurationException e) {
            throw new DataFormatException("Parse error (ParserConfigurationException)");
        } catch (SAXException e) {
            throw new DataFormatException("Parse error (SAXException)");
        } catch (IOException e) {
            throw new DataFormatException("Parse error (IOException)");
        }
    }

    public void ptrToTokens(String ptr, List<String> tokens){
    	String[] parts = ptr.split("#");
    	if ( ptr.startsWith("#") ){
    		for ( String element : this.wordsIdsMap.get(parts[1]) ){
    			this.ptrToTokens(element, tokens);
    		}
    	}
    	else{
    		tokens.add(parts[1]);
    	}
    }
    
    @Override
    public InputSource resolveEntity (String publicId, String systemId){
        return new InputSource(new StringReader(""));
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
        if (elementName.equalsIgnoreCase(TAG_PARAGRAPH)) {
            this.currentParagraph = paragraphs.get(currentParagraphIdx++);
            this.currentSentenceIdx = 0;
        }
        else if (elementName.equalsIgnoreCase(TAG_SENTENCE)) {
            this.currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
        }
        else if (elementName.equalsIgnoreCase(TAG_PTR)) {
        	this.currentWordTokens.add(attributes.getValue("target"));
        }
        else if (elementName.equalsIgnoreCase(TAG_FEATURE)) {
        	/* Enter tag <f name="..."> */
            this.currentFeatureName = attributes.getValue("name");
        }
        else if (elementName.equalsIgnoreCase(TAG_SYMBOL)) {
        	/* Get value from:
        	 * <f name="ctag">
        	 *   <symbol value="..."/>
        	 * </f>
        	 */
            if (currentFeatureName.equals("ctag")){
                currentAnnotationType =  attributes.getValue("value");
            }
        }        
        else if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
        	this.currentWordId = attributes.getValue("xml:id");
        	this.currentWordTokens = new LinkedList<String>();
            this.currentAnnotationType = null;
            this.wordsTokensId.put(this.currentWordId, this.currentWordTokens);
            this.wordsIdsMap.put(this.currentWordId, this.currentWordTokens);
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        if (element.equals(TAG_SEGMENT)) {
        	SentenceWord word = new SentenceWord(
        			this.currentWordId, 
        			this.currentSentence, 
        			this.currentAnnotationType, 
        			this.currentWordTokens); 
        	this.words.add(word);
        }
        else if (element.equalsIgnoreCase(TAG_FEATURE)) {
        	/* Exit tag <f name="..."> */
            currentFeatureName = null;
        }
    }

    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
    }

    private void resolvePtrToWords(){
    	HashMap<String, List<String>> newMap = new HashMap<String, List<String>>();
    	for ( String key : this.wordsIdsMap.keySet() ){
    		List<String> newList = new LinkedList<String>();
    		for ( String element : this.wordsIdsMap.get(key) ){
    			this.ptrToTokens(element, newList);
    		}
    		newMap.put(key, newList);
    	}
    	this.wordsIdsMap = newMap;
    }
    

    /**
     * Assign a token index to the annotations after loading all words.
     */
    private void makeAnnotations(){
    	for ( SentenceWord word : this.words ){
    		TreeSet<Integer> tokens = new TreeSet<Integer>();
    		for ( String tokenKey : this.wordsIdsMap.get(word.getWordId())){
    			tokens.add(this.tokenIdsMap.get(tokenKey));
    		}
    		Annotation an = new Annotation(tokens, word.getType(), word.getSentence());
    		an.setCategory("word");
    		word.getSentence().addChunk(an);
    	}
    }
    
    /**
     * Auxiliary class to store `word` data.
     */
    class SentenceWord{
    	
    	private String wordId = null;
    	private Sentence sentence = null;
    	private String type = null;
    	private List<String> keys = null;
    	
    	public SentenceWord(String wordId, Sentence sentence, String type, List<String> keys){
    		this.wordId = wordId;
    		this.sentence = sentence;
    		this.type = type;
    		this.keys = keys;
    	}
    	
    	public String getWordId(){
    		return this.wordId;
    	}
    	
    	public Sentence getSentence(){
    		return this.sentence;
    	}
    	
    	public String getType(){
    		return this.type;
    	}
    	
    	public List<String> getKeys(){
    		return this.keys;
    	}
    	
    }
}
