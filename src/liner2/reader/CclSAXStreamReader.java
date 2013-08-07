package liner2.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException; 
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

import liner2.structure.TokenAttributeIndex;
import liner2.structure.Annotation;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

import liner2.tools.DataFormatException;

public class CclSAXStreamReader extends StreamReader {
	
	private TokenAttributeIndex attributeIndex;
	private int currIndex=0;
	private CclSaxParser parser_out;
	
	public CclSAXStreamReader(InputStream is) throws DataFormatException {
		this.attributeIndex = new TokenAttributeIndex();
		this.attributeIndex.addAttribute("orth");
		this.attributeIndex.addAttribute("base");
		this.attributeIndex.addAttribute("ctag");
		this.parser_out = new CclSaxParser(is, this.attributeIndex);
	}
	
	@Override
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	@Override
	public void close() throws DataFormatException {
		
	}	
		
	@Override
	public boolean paragraphReady() throws DataFormatException {
		if (currIndex<parser_out.paragraphs.size())
			return true;
		else
			return false;
	}
	
	@Override
	protected Paragraph readRawParagraph() throws DataFormatException {
		if (!paragraphReady())
			return null;
		return parser_out.paragraphs.get(currIndex++);
	}
	
}

