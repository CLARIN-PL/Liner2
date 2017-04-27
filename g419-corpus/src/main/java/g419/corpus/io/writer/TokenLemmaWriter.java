package g419.corpus.io.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

/**
 * Print tokens lemma separated by a space. Each sentence is printed in a single line.
 * 
 * @author Michał Marcińczuk
 */
public class TokenLemmaWriter extends AbstractDocumentWriter {
	private BufferedWriter ow;
		
	public TokenLemmaWriter(OutputStream os) {
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
			for (Sentence s : paragraph.getSentences()){
				writeSentence(s);
			}
		}
	}

	private void writeSentence(Sentence sentence) {
		StringBuilder sb = new StringBuilder();
		try {
			for ( Token t : sentence.getTokens() ){
				sb.append(t.getDisambTag().getBase());
				sb.append(" ");
			}
			this.ow.write(sb.toString().trim());
			this.ow.newLine();
		} catch (IOException ex) {
			Logger.getLogger(this.getClass()).error("An error occured while writing the data.", ex);
		}
	}

	@Override
	public void flush() {
		try {
			ow.flush();
		} catch (IOException ex) {
			Logger.getLogger(this.getClass()).error("An error occured while flushing the writer.", ex);
		}		
	}
}
