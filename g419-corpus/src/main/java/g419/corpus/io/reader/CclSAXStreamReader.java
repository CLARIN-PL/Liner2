package g419.corpus.io.reader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.CclRelationSaxParser;
import g419.corpus.io.reader.parser.CclSaxParser;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;


public class CclSAXStreamReader extends AbstractDocumentReader {
	
	private Document document;
	
	public CclSAXStreamReader(String uri, InputStream cclDocument, InputStream cclDescriptor, InputStream cclRelations) throws DataFormatException, ParserConfigurationException, SAXException, IOException {
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
	public void close() {}

	@Override
	public Document nextDocument() throws DataFormatException, IOException {
		Document document = this.document;
		this.document = null;
		return document;
	}

	@Override
	public boolean hasNext(){
		return document!=null;
	}
			
}

