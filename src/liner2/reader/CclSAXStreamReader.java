package liner2.reader;

import java.io.InputStream;

import liner2.reader.parser.CclSaxParser;

import liner2.structure.TokenAttributeIndex;
import liner2.structure.Paragraph;

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
		if (currIndex<parser_out.getParagraphs().size())
			return true;
		else
			return false;
	}
	
	@Override
	protected Paragraph readRawParagraph() throws DataFormatException {
		if (!paragraphReady())
			return null;
		return parser_out.getParagraphs().get(currIndex++);
	}
	
}

