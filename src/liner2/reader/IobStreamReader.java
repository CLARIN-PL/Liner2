package liner2.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;

import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class IobStreamReader extends StreamReader {
	
	private BufferedReader ir;
	private String nextParagraphId = null;
	
	public IobStreamReader(String filename){
		if ((filename != null) && (!filename.isEmpty())) {
			try {
				this.ir = new BufferedReader(
					new FileReader(filename));
			} catch (IOException ex) {
				ex.printStackTrace();
				ir = new BufferedReader(
					new InputStreamReader(System.in));
			}
		}
		else
			this.ir = new BufferedReader(
				new InputStreamReader(System.in));
	}

	@Override
	public void close() {
		try {
			ir.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Paragraph readParagraph() {
		if (nextParagraphId == null)
			readParagraphHeader();
		if (nextParagraphId == null)
			return null;
			
		Paragraph paragraph = new Paragraph(nextParagraphId);
		nextParagraphId = null;
		Sentence currentSentence = new Sentence();
		Chunk currentChunk = null;
		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					paragraph.addSentence(currentSentence);
					return paragraph;
				}		
				line = ir.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			if (line.trim().isEmpty()) {
				paragraph.addSentence(currentSentence);
				currentSentence = new Sentence();
			}
			else {
				String[] words = line.trim().split(" ");
				if ((words[0].equals("-DOCSTART")) &&
					(words[1].equals("FILE"))) {
					nextParagraphId = words[2];
					return paragraph;
				}
				else {
					currentSentence.addToken(createToken(words));
					if (words.length > 3) {
						if (words[3].startsWith("B")) {
							int idx = currentSentence.getTokenNumber()-1;
							currentChunk = new Chunk(idx, idx, words[3].substring(2), currentSentence);
							currentSentence.addChunk(currentChunk);
						}
						else if (words[3].startsWith("I")) {
							if (currentChunk != null) {
								int idx = currentSentence.getTokenNumber()-1;
								currentChunk.setEnd(idx);
							}
						}
					}
				}
			}
		}
	}
	
	private void readParagraphHeader() {
		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					nextParagraphId = null;
					return;
				}
				line = ir.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			String[] words = line.trim().split(" ");
			if ((words[0].equals("-DOCSTART")) &&
				(words[1].equals("FILE"))) {
				nextParagraphId = words[2];
				return;
			}
		}
	}
	
	private Token createToken(String[] words) {
		Token token = new Token();
		token.setAttributeValue(0, words[0]);
		token.addTag(new Tag(words[1], words[2], false));
		return token;
	}
}
