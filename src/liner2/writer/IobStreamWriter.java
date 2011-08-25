package liner2.writer;

import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;

import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class IobStreamWriter extends StreamWriter {

	private BufferedWriter ow;
	private boolean open = false;

	public IobStreamWriter(String filename) {
		if ((filename != null) && (!filename.isEmpty())) {
			try {
				this.ow = new BufferedWriter(
					new FileWriter(filename));
			} catch (IOException ex) {
				ex.printStackTrace();
				ow = new BufferedWriter(
					new OutputStreamWriter(System.out));
			}
		}
		else
			this.ow = new BufferedWriter(
				new OutputStreamWriter(System.out));
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
		for (Token token : sentence.getTokens())
			writeToken(token);
		ow.newLine();
	}
	
	private void writeToken(Token token) throws IOException {
		String line = token.getFirstValue();
		ArrayList<Tag> tags = token.getTags();
		Tag firstTag = tags.get(0);
		line += " " + firstTag.getBase() + " " + firstTag.getCtag();
		ow.write(line, 0, line.length());
		ow.newLine();
	}
}
