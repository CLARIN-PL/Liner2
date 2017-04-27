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
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/28/13
 * Time: 8:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnGroupsSAXParser extends DefaultHandler {

    private final String TAG_PARAGRAPH		= "p";
    private final String TAG_SENTENCE		= "s";
    private final String TAG_SEGMENT		= "seg";
    private final String TAG_FEATURESET	= "fs";
    private final String TAG_FEATURE		= "f";
    private final String TAG_STRING	    = "string";
    private final String TAG_SYMBOL	    = "symbol";
    private final String TAG_POINTER	   	= "ptr";
    private final String TAG_ID 			= "xml:id";

    InputStream is = null;
    ArrayList<Paragraph> paragraphs = null;
    ArrayList<Integer> annotatedTokens = null;
    
    int currentParagraphIdx = 0;
    int currentSentenceIdx = 0;
    Paragraph currentParagraph = null;
    Sentence currentSentence = null;
    String currentGroupId = null;
    String currentAnnotationType = null;
    String currentAnnotationBase = null;
    String currentFeatureName = null;
    List<String> currentGroupTokens = null;
    String tmpValue = "";
    
    Map<String, Integer> tokenIdsMap = null;
    Map<String, List<String>> wordsIdsMap = null;
    Map<String, String> headIds = new HashMap<String, String>();
        
    /**
     * Map of group names and their words and groups.
     */
    Map<String, List<String>> groupsTokensId = new HashMap<String, List<String>>();
    
    /**
     * Collection of parser <group> elements.
     */    
    List<SentenceGroup> groups = new LinkedList<SentenceGroup>();

    public AnnGroupsSAXParser(InputStream is, ArrayList<Paragraph> paragraphs, Map<String,Integer> tokenIdsMap, Map<String, List<String>> wordsIdsMap) throws DataFormatException {
        this.is = is;
        this.paragraphs = paragraphs;
        this.tokenIdsMap = tokenIdsMap;
        this.wordsIdsMap = wordsIdsMap;
        parseDocument();
    }

    private void parseDocument() throws DataFormatException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(is,this);
            this.makeAnnotations();
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
    	this.tmpValue = "";
        if (elementName.equalsIgnoreCase(TAG_PARAGRAPH)) {
            currentParagraph = paragraphs.get(currentParagraphIdx++);
            currentSentenceIdx = 0;
        }
        else if (elementName.equalsIgnoreCase(TAG_SENTENCE)) {
            currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
        }
        else if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
        	this.currentGroupId = attributes.getValue("xml:id");
        	this.currentGroupTokens = new LinkedList<String>();
            this.currentAnnotationType = null;
            this.currentAnnotationBase = null;
            this.groupsTokensId.put(this.currentGroupId, this.currentGroupTokens);
        }
        else if (elementName.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = attributes.getValue("name");
        }
        else if (elementName.equalsIgnoreCase(TAG_SYMBOL)) {
            if (currentFeatureName.equals("type")){
                currentAnnotationType =  attributes.getValue("value");
            }
            else if (currentFeatureName.equals("subtype")){
                currentAnnotationType +=  "-" + attributes.getValue("value");
            }
        }
        else if (elementName.equalsIgnoreCase(TAG_POINTER)) {
            String target = attributes.getValue("target");
            String type = attributes.getValue("type");
            this.currentGroupTokens.add(target);
            if ( "head".equals(type) || "semh".equals(type) ){
            	this.headIds.put(this.currentGroupId, target);
            }
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        if ( TAG_SEGMENT.equals(element) ) {        	
        	this.groups.add(
        			new SentenceGroup(
        					this.currentGroupId, 
        					this.currentSentence, 
        					this.currentAnnotationType, 
        					this.currentAnnotationBase,
        					this.currentGroupTokens));
        }
        else if ( TAG_STRING.equals(element) && "base".equals(this.currentFeatureName) ){
        	this.currentAnnotationBase = this.tmpValue;
        }
        else if (element.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = null;
        }        
    }

    public ArrayList<Paragraph> getParagraphs(){
        return paragraphs;
    }

    /**
     * Assign token index to annotations after loading all groups.
     */
    private void makeAnnotations(){
    	for ( SentenceGroup group : this.groups ){
    		TreeSet<Integer> tokens = new TreeSet<Integer>();
    		for ( String elementKey : group.getKeys() ){
    			List<Integer> ids = this.getTokens(elementKey);
    			if ( ids == null ){
    				//Logger.getLogger(this.getClass()).warn("No tokens found for " + elementKey);
    			}
    			else{
    				tokens.addAll(ids);
    			}
    		}
    		Annotation an = new Annotation(tokens, group.getType(), group.getSentence());
    		an.setGroup("group");
    		an.setHead(this.getHead(group.getGroupId()));
    		an.setLemma(group.getBase());
    		group.getSentence().addChunk(an);
    	}
    }
    
    private List<Integer> getTokens(String elementKey){
    	String[] cols = elementKey.split("#");
		List<Integer> tokensIds = new LinkedList<Integer>();
    	if ( cols.length == 2 && cols[0].equals("ann_words.xml") ){
    		for ( String tokenKey : this.wordsIdsMap.get(cols[1]) ){
    			tokensIds.add(this.tokenIdsMap.get(tokenKey));
    		}
    	}
    	else{
    		for ( String group : this.groupsTokensId.get(cols[1])){
    			tokensIds.addAll(this.getTokens(group));
    		}
    	}
		return tokensIds;
    }
    
    private Integer getHead(String elementKey){
        if(elementKey == null) return 0;
        String[] cols = elementKey.split("#");
    	if ( elementKey.startsWith("ann_words.xml") ){
    		return this.tokenIdsMap.get(this.wordsIdsMap.get(cols[1]).get(0));
    	}
    	else{
    		return this.getHead(this.headIds.get( cols[cols.length-1] ));
    	}
    }
    
    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
        for(int i=start;i<start+length;i++){
            tmpValue += ac[i];
        }
    }
    
    class SentenceGroup{
    	
    	private String groupId = null;
    	private Sentence sentence = null;
    	private String type = null;
    	private String base = null;
    	private List<String> keys = null;
    	
    	public SentenceGroup(String groupId, Sentence sentence, String type, String base, List<String> keys){
    		this.groupId = groupId;
    		this.sentence = sentence;
    		this.base = base;
    		this.type = type;
    		this.keys = keys;
    	}
    	
    	public String getGroupId(){
    		return this.groupId;
    	}
    	
    	public Sentence getSentence(){
    		return this.sentence;
    	}
    	
    	public String getType(){
    		return this.type;
    	}
    	
    	public String getBase(){
    		return this.base;
    	}
    	
    	public List<String> getKeys(){
    		return this.keys;
    	}
    	
    }
}
