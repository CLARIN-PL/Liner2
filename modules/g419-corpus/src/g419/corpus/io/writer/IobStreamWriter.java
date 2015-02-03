package g419.corpus.io.writer;

import g419.corpus.TerminateException;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class IobStreamWriter extends AbstractDocumentWriter {

	private BufferedWriter ow;
	private boolean init = false;

	public IobStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	protected void init(TokenAttributeIndex attributeIndex) {
		if (this.init)
			return;
		try {
			String line = "-DOCSTART CONFIG FEATURES";
			for (int i = 0; i < attributeIndex.getLength(); i++)
				line += " " + attributeIndex.getName(i);
			ow.write(line, 0, line.length());
			ow.newLine();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		this.init = true;
	}

	@Override
	public void flush() {
		try {
			ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void close() {
		try {
			ow.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void writeDocument(Document document){
		for (Paragraph paragraph : document.getParagraphs())
			this.writeParagraph(paragraph);
	}

	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!init)
				init(paragraph.getAttributeIndex());
			String paragraphId = paragraph.getId();
			if (paragraphId == null)
				paragraphId = "";
			String header = "-DOCSTART FILE "+ paragraphId;
			ow.write(header, 0, header.length());
			ow.newLine();
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(sentence);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeSentence(Sentence sentence) throws IOException {
		ArrayList<Token> tokens = sentence.getTokens();
		for (int i = 0; i < tokens.size(); i++) {
			writeToken(i, tokens.get(i), sentence);
		}
		ow.newLine();
	}
	
	private void writeToken(int idx, Token token, Sentence sentence) 
		throws IOException {
		String line = "";
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++)
			try{
				line += (line.length() > 0 ? " " : "") + token.getAttributeValue(i);
			}
			catch (IndexOutOfBoundsException e) {
				throw new TerminateException(String.format(
						"Token attribute with index %d not found in [%s]", i, token.getAttributesAsString()));
			}

        line += " " + sentence.getTokenClassLabel(idx, null);
		ow.write(line, 0, line.length());
		ow.newLine();
	}
}
