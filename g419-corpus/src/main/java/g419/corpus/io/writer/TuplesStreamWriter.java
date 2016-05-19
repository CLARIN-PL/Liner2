package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


/*
 * Drukowanie wyników w postaci listy krotek.
 * @author Maciej Janicki
 */
public class TuplesStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow;
	private int sentenceOffset = 0;
	
	public TuplesStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
	public void close() {
		try {
			this.ow.flush();//close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void writeDocument(Document document){
        this.sentenceOffset = 0;
		for (Paragraph paragraph : document.getParagraphs())
			this.writeParagraph(paragraph);
		try {
			this.ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeParagraph(Paragraph paragraph) {
		for (Sentence s : paragraph.getSentences())
			writeSentence(s);
	}

	private void writeSentence(Sentence sentence) {
		try {
			Annotation[] chunks = Annotation.sortChunks(sentence.getChunks());
			for (Annotation c : chunks) 
				writeChunk(c, sentence);			
			
			for (Token t : sentence.getTokens())
				this.sentenceOffset += t.getOrth().length();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void writeChunk(Annotation c, Sentence s) throws IOException {
		int begin = this.sentenceOffset;
		int end = this.sentenceOffset;
		ArrayList<Token> tokens = s.getTokens();
		for (int i = 0; i < c.getBegin(); i++)
			begin += tokens.get(i).getOrth().length();
		end = begin;
		for (int i = c.getBegin(); i <= c.getEnd(); i++)
			end += tokens.get(i).getOrth().length();
		this.ow.write("(" + begin + "," + (end-1) + "," + c.getType() + ",\"" + c.getText() + "\")\n");
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
