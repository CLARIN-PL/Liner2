package liner2.reader;

import java.io.IOException;
import java.io.InputStream;

import liner2.reader.parser.CclSaxParser;
import liner2.structure.Document;
import liner2.structure.TokenAttributeIndex;
import liner2.tools.DataFormatException;

public class CclSAXStreamReader extends AbstractDocumentReader {
	
	private Document document;
	
	public CclSAXStreamReader(String uri, InputStream is) throws DataFormatException {
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		attributeIndex.addAttribute("orth");
		attributeIndex.addAttribute("base");
		attributeIndex.addAttribute("ctag");
		CclSaxParser parser_out = new CclSaxParser(uri, is, attributeIndex);
		this.document = parser_out.getDocument();		
	}
	
	@Override
	public TokenAttributeIndex getAttributeIndex() {
		return this.document.getAttributeIndex();
	}
	
	@Override
	public void close() throws DataFormatException {
		
	}

	@Override
	public Document nextDocument() throws DataFormatException, IOException {
		return this.document;
	}	
			
}

