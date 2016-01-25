package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.CclRelationSaxParser;
import g419.corpus.io.reader.parser.CclSaxParser;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;


public class CclSAXStreamReader extends AbstractDocumentReader {
	
	private Document document;
	
	public CclSAXStreamReader(String uri, InputStream cclDocument, InputStream cclDescriptor, InputStream cclRelations) throws DataFormatException, ParserConfigurationException, SAXException, IOException {
		Logger.getLogger(this.getClass()).info(cclDocument.getClass().toString());
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		attributeIndex.addAttribute("orth");
		attributeIndex.addAttribute("base");
		attributeIndex.addAttribute("ctag");
		CclSaxParser parser_out = new CclSaxParser(uri, cclDocument, attributeIndex);
		if(cclRelations != null){
			CclRelationSaxParser parser_rel = new CclRelationSaxParser(uri, cclRelations, parser_out.getDocument()); 
			this.document = parser_rel.getDocument();
		}
		else{
			this.document = parser_out.getDocument();
		}
		this.document.setUri(uri);
		if (cclDescriptor != null){
			new CclDescriptorReader().enhanceDocument(document, cclDescriptor);
		}
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

