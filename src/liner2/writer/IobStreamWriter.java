package liner2.writer;

import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class IobStreamWriter extends StreamWriter {

	private BufferedWriter ow;
	private boolean open = false;

	public IobStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	public void open() {
		if (open)
			return;
		try {
			String line = "-DOCSTART CONFIG FEATURES orth base ctag";
			ow.write(line, 0, line.length());
			ow.newLine();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		open = true;
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
	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!open)
				open();
			String header = "-DOCSTART FILE "+paragraph.getId();
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
		String line = token.getFirstValue();
		ArrayList<Tag> tags = token.getTags();
		Tag firstTag = tags.get(0);
		line += " " + firstTag.getBase() + " " + firstTag.getCtag();
		Chunk chunk = sentence.getChunkAt(idx);
		if (chunk == null)
			line += " O";
		else {
			if (idx == chunk.getBegin())
				line += " B-" + chunk.getType();
			else
				line += " I-" + chunk.getType();
		}
		ow.write(line, 0, line.length());
		ow.newLine();
	}
}
