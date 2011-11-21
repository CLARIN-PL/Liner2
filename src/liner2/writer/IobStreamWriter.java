package liner2.writer;

import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class IobStreamWriter extends StreamWriter {

	private BufferedWriter ow;
	private boolean init = false;

	public IobStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	protected void init(AttributeIndex attributeIndex) {
		if (this.init)
			return;
		try {
//			String line = "-DOCSTART CONFIG FEATURES orth base ctag";
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
			line += (line.length() > 0 ? " " : "") + token.getAttributeValue(i);
		
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
