package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.CclSaxParser;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
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
    	// TODO obejście zapewniające, że każde zdanie ma unikalny identyfikator
		// Unikalny identyfikator jest potrzeby do poprawnego testowania dokumentów
		// z użyciem klonowania. Trzeba pomyśleć, jak to zrobić, aby nie nadpisywać
		// istniejących identyfikatorów, jeżeli zostały podane. Może trzeba by sprawdzić
		// po wczytaniu, czy są ustawione uniklalne identyfikatory.
        for(Sentence sent: this.document.getSentences()){
            sent.setId("" + sent.hashCode());
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

