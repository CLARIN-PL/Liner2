package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

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
 * Date: 9/2/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnSegmentationSAXParser extends DefaultHandler {

    private final String TAG_PARAGRAPH		= "p";
    private final String TAG_SENTENCE		= "s";
    private final String TAG_SEGMENT        = "seg";

    InputStream is;
    ArrayList<Paragraph> paragraphs;
    Token currentToken;
    Sentence currentSentence;
    Paragraph currentParagraph;
    int currentTokenIdx;
    int currentParagraphIdx;
    int currentSentenceIdx;

    public AnnSegmentationSAXParser(InputStream is, ArrayList<Paragraph> paragraphs) throws DataFormatException {
        this.is = is;
        this.paragraphs = paragraphs;
        parseDocument();
    }

    private void parseDocument() throws DataFormatException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            currentSentence = paragraphs.get(0).getSentences().get(0);
            parser.parse(is,this);
        } catch (ParserConfigurationException e) {
            throw new DataFormatException("Parse error (ParserConfigurationException)");
        } catch (SAXException e) {
        	System.err.println(e.getMessage());
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
            currentTokenIdx = 0;
        }
        else if (elementName.equalsIgnoreCase(TAG_SEGMENT)) {
            if (attributes.getValue("nkjp:nps") != null) {
                currentToken.setNoSpaceAfter(true);
            }
            currentToken = currentSentence.getTokens().get(currentTokenIdx++);
        }

    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
    }

    public ArrayList<Paragraph> getParagraphs(){
        return paragraphs;
    }
}
