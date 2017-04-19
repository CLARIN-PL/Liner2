package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/*
 * Drukowanie wyników w postaci listy tokenów.
 * @author Dominik Piasecki
 */
public class TokensStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow;
	
	public TokensStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
	public void close() {
		try {
			this.ow.flush();
			this.ow.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void writeDocument(Document document){
		for (Paragraph paragraph : document.getParagraphs()){
			this.writeParagraph(paragraph);
		}
	}

	public void writeParagraph(Paragraph paragraph) {
		for (Sentence s : paragraph.getSentences()){
			writeSentence(s);
		}
	}

	private void writeSentence(Sentence sentence) {
		String response ="";
		try {
			for (Annotation c : sentence.getChunks()) {
				response += String.format("[%d,%d,%s]", c.getBegin()+1, c.getEnd()+1,
					c.getType());
			}
			if (response.isEmpty()){
				response = "NONE";
			}
			this.ow.write(response);
			this.ow.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void flush() {
		try {
			ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
