package liner2.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;

import liner2.chunker.Chunker;
import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Token;

/*
 * Drukowanie wyników w postaci listy tokenów.
 * @author Dominik Piasecki
 */
public class TokensStreamWriter extends StreamWriter {
	private BufferedWriter ow;
	
	public TokensStreamWriter(OutputStream os) {
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
		close();
	}

	private void writeSentence(Sentence sentence) {
		String response ="";
		try {
			for (Chunk c : sentence.getChunks()) {
				response += String.format("[%d,%d,%s]", c.getBegin()+1, c.getEnd()+1,
					c.getType());
			}
			if (response.isEmpty())
				response = "NONE";
			this.ow.write(response);
			this.ow.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
