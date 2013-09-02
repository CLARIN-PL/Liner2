package liner2.reader.parser.tei;

import liner2.structure.*;
import liner2.tools.DataFormatException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/26/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnMorphosyntaxSAXParser extends DefaultHandler{

    private final String TAG_PARAGRAPH		= "p";
    private final String TAG_SENTENCE		= "s";
    private final String TAG_SEGMENT		= "seg";
    private final String TAG_FEATURESET		= "fs";
    private final String TAG_FEATURE		= "f";
    private final String TAG_STRING	    	= "string";
    private final String TAG_SYMBOL	    	= "symbol";
    private final String TAG_VALT	    	= "ptr";
    private final String TAG_ID 			= "xml:id";

    ArrayList<Paragraph> paragraphs;
    Paragraph currentParagraph = null;
    Sentence currentSentence = null;
    HashMap<String,Integer> tokenIdsMap;
    HashMap<String,Tag> currentTokenTags;
    Token currentToken = null;
    String currentFeatureName = "";
    String tmpBase = null;
    String tmpCtag = null;
    String tmpMsd = null;
    InputStream is;
    String tmpValue;
    String disambTagId;
    int idx =0;
    boolean foundDisamb;
    TokenAttributeIndex attributeIndex;

    public AnnMorphosyntaxSAXParser(InputStream is, TokenAttributeIndex attributeIndex) throws DataFormatException {
        this.is = is;
        this.attributeIndex = attributeIndex;
        paragraphs = new ArrayList<Paragraph>();
        tokenIdsMap = new HashMap<String, Integer>();
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
        tmpValue = "";
        if (elementName.equalsIgnoreCase(TAG_PARAGRAPH)) {
            currentParagraph = new Paragraph(attributes.getValue(TAG_ID));
            currentParagraph.setAttributeIndex(attributeIndex);
        }
        else if (elementName.equalsIgnoreCase(TAG_SENTENCE)) {
            currentSentence = new Sentence();
            idx =0;
            currentSentence.setId(attributes.getValue(TAG_ID));
        }
        else if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
            currentToken = new Token();
            currentTokenTags = new HashMap<String, Tag>();
            tokenIdsMap.put(attributes.getValue(TAG_ID),idx++);
            currentToken.setId(attributes.getValue(TAG_ID));
        }
        else if (elementName.equalsIgnoreCase(TAG_FEATURE)) {
            if(currentFeatureName.equals("disamb") && attributes.getValue("name").equals("choice")){
                disambTagId =  attributes.getValue("fVal").replace("#","");
            }
            else {
                currentFeatureName = attributes.getValue("name");
            }
        }
        else if (elementName.equalsIgnoreCase(TAG_FEATURESET)) {
            if(currentFeatureName.equals("disamb")){
                currentToken.setAttributeValue(attributeIndex.getIndex("tagTool"), attributes.getValue("feats"));
            }
        }
        else if (elementName.equalsIgnoreCase(TAG_SYMBOL)) {
            if (currentFeatureName.equals("ctag")) {
                tmpCtag =  attributes.getValue("value");
            }
            else if (currentFeatureName.equals("msd")) {
                String ctag = tmpCtag+":"+attributes.getValue("value");
                currentTokenTags.put(attributes.getValue(TAG_ID), new Tag(tmpBase, ctag, false));
            }
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        if (element.equalsIgnoreCase(TAG_PARAGRAPH)) {
            paragraphs.add(currentParagraph);
        }
        else if (element.equalsIgnoreCase(TAG_SENTENCE)) {
            currentParagraph.addSentence(currentSentence);
        }
        else if (element.equals(TAG_SEGMENT)) {
            Tag disambTag = currentTokenTags.get(disambTagId);
            disambTag.setDisamb(true);
            currentToken.addTag(disambTag);
            for(Tag tag: currentTokenTags.values()){
                currentToken.addTag(tag);
            }
            currentSentence.addToken(currentToken);
        }
        else if (element.equalsIgnoreCase(TAG_FEATURE)) {
            currentFeatureName = "";
        }
        else if (element.equalsIgnoreCase(TAG_STRING)) {
            if(currentFeatureName.equals("orth")){
                currentToken.setAttributeValue(0, tmpValue);
            }
            else if (currentFeatureName.equals("base")) {
                tmpBase = tmpValue;
            }
        }


    }

    @Override
    public void characters(char[] ac, int start, int length) throws SAXException {
        for(int i=start;i<start+length;i++)
            tmpValue += ac[i];
    }

    public ArrayList<Paragraph> getParagraphs(){
        return paragraphs;
    }

    public HashMap<String,Integer> getTokenIdsMap(){
        return tokenIdsMap;
    }

}
