package g419.spatial.io;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.spatial.structure.NodeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConllDocumentReader extends AbstractDocumentReader {

	private BufferedReader ir = null;
	private TokenAttributeIndex index = null;
	
	/**
	 * Create reader for a file in ConLL format.
	 * @param is Data input stream
	 */
	public ConllDocumentReader(InputStream is){
		this.ir = new BufferedReader(new InputStreamReader(is));
		
		this.index = new TokenAttributeIndex();
		this.index.addAttribute("orth");
		this.index.addAttribute("base");
		this.index.addAttribute("ctag");
		this.index.addAttribute("pos2");
		this.index.addAttribute("parent");
		this.index.addAttribute("relation");
	}
	
	@Override
	public void close() throws DataFormatException {
		if ( this.ir != null )
			try {
				this.ir.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	protected TokenAttributeIndex getAttributeIndex() {
		return this.index;
	}

	@Override
	public Document nextDocument() throws Exception {
		Document document = new Document("ConllDocumentReader", this.index);
		Paragraph paragraph = new Paragraph("p1");
		document.addParagraph(paragraph);
		
		String line = null;
		int sentenceId = 1;
		Sentence sentence = new Sentence();
		while ( (line = this.ir.readLine()) != null ){
			line = line.trim();
			if ( line.length() == 0){
				if ( sentence != null && sentence.getTokenNumber() > 0)
					paragraph.addSentence(sentence);
				sentence = new Sentence();
				sentence.setId("s" + (sentenceId++));
			}
			else{
				String[] fields = line.split("\t");
				if ( fields.length < 8 )
					throw new Exception("Conll format exception for line: " + line);
				NodeToken token = new NodeToken(this.index);
				// Orth
				token.setAttributeValue(0, fields[1]);
				// Base
				token.setAttributeValue(1, fields[2]);
				// Ctag
				token.setAttributeValue(2, 
						fields[3] + (fields[5].equals("_") ? "" : (":" + fields[5].replace("|",  ":"))));
				// Pos2
				token.setAttributeValue(3, fields[4]);
				// Parent
				token.setAttributeValue(4, fields[6]);
				// Relation
				token.setAttributeValue(5, fields[7]);
				
				sentence.getTokens().add(token);
			}
		}
		if ( sentence != null && sentence.getTokenNumber() > 0 )
			paragraph.addSentence(sentence);
		
		// Setup children i parent nodes
		int parentAttribute = 4;
		for (Sentence s : paragraph.getSentences() )
			for (Token token : s.getTokens()){
				int parentIndex = Integer.parseInt(token.getAttributeValue(parentAttribute)) - 1;
				if ( parentIndex >=0 ){
					((NodeToken) token).setParent((NodeToken) s.getTokens().get(parentIndex));
					((NodeToken) s.getTokens().get(parentIndex)).addChild((NodeToken) token);
				}
			}

		return document;
	}

}
