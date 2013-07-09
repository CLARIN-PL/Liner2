package liner2.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;

import liner2.structure.Annotation;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Token;

/*
 * Drukowanie wyników w postaci listy krotek.
 * @author Maciej Janicki
 */
public class TuplesStreamWriter extends StreamWriter {
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
				this.sentenceOffset += t.getFirstValue().length();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void writeChunk(Annotation c, Sentence s) throws IOException {
		int begin = this.sentenceOffset;
		int end = this.sentenceOffset;
		ArrayList<Token> tokens = s.getTokens();
		for (int i = 0; i < c.getBegin(); i++)
			begin += tokens.get(i).getFirstValue().length();
		end = begin;
		for (int i = c.getBegin(); i <= c.getEnd(); i++)
			end += tokens.get(i).getFirstValue().length();

		this.ow.write("(" + begin + "," + (end-1) + "," + c.getType() + ",\"" + c.getText() + "\")\n");
	}
}
