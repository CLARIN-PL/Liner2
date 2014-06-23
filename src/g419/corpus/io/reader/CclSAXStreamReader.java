package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.CclSaxParser;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.IOException;
import java.io.InputStream;


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
		Document doc = this.document;
		this.document = null;
		return doc;
	}	
			
}

