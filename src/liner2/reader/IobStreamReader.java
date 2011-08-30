package liner2.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class IobStreamReader extends StreamReader {
	
	private BufferedReader ir;
	private String nextParagraphId = null;
	private boolean nextParagraph = false;
	
	public IobStreamReader(InputStream is) {
		this.ir = new BufferedReader(new InputStreamReader(is));
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
	public boolean paragraphReady() {
		if (this.nextParagraph)
			return true;
		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					this.nextParagraphId = null;
					this.nextParagraph = false;
					return false;
				}
				line = ir.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			String[] words = line.trim().split(" ");
			if (words.length < 2)
				continue;
			if ((words[0].equals("-DOCSTART")) &&
				(words[1].equals("FILE"))) {
				if (words.length >= 3)
					this.nextParagraphId = words[2];
				this.nextParagraph = true;
				return true;
			}
		}
	}

	@Override
	protected Paragraph readRawParagraph() {
//		if (nextParagraphId == null)
//			readParagraphHeader();
//		if (nextParagraphId == null)
//			return null;

		if (!paragraphReady())
			return null;
			
		// initialize attributes index
		AttributeIndex attributeIndex = new AttributeIndex();
		attributeIndex.addAttribute("orth");
		attributeIndex.addAttribute("base");
		attributeIndex.addAttribute("ctag");
			
		Paragraph paragraph = new Paragraph(nextParagraphId);
		this.nextParagraphId = null;
		this.nextParagraph = false;
		Sentence currentSentence = new Sentence();
		Chunk currentChunk = null;

		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					if (currentSentence.getTokenNumber() > 0) {
						currentSentence.setAttributeIndex(attributeIndex);
						paragraph.addSentence(currentSentence);
					}
					return paragraph;
				}		
				line = ir.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			if (line.trim().isEmpty()) {
				if (currentSentence.getTokenNumber() > 0) {
					currentSentence.setAttributeIndex(attributeIndex);
					paragraph.addSentence(currentSentence);
					currentSentence = new Sentence();
				}
			}
			else {
				String[] words = line.trim().split(" ");
				if ((words[0].equals("-DOCSTART")) &&
					(words[1].equals("FILE"))) {
					if (words.length >= 3)
						this.nextParagraphId = words[2];
					this.nextParagraph = true;
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
