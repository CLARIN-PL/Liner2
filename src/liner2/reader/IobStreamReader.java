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
	private AttributeIndex attributeIndex = null;
	private String nextParagraphId = null;
	private boolean nextParagraph = false;
	private boolean init = false;
	
	public IobStreamReader(InputStream is) {
		this.ir = new BufferedReader(new InputStreamReader(is));
	}

	/**
	 * Read -DOCSTART CONFIG FEATURES line.
	 */
	protected void init() {
		if (this.init)
			return;
		while (true) {
			String line = null;
			try {
				line = ir.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			if ((line == null) || (!line.startsWith("-DOCSTART CONFIG FEATURES")))
				continue;
			this.attributeIndex = new AttributeIndex();
			String[] content = line.trim().split(" ");
			for (int i = 3; i < content.length; i++)
				this.attributeIndex.addAttribute(content[i]);
			this.init = true;
			return;
		}
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
		if (!this.init)
			init();
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
	protected AttributeIndex getAttributeIndex() {
		return this.attributeIndex;
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
//		AttributeIndex attributeIndex = new AttributeIndex();
//		attributeIndex.addAttribute("orth");
//		attributeIndex.addAttribute("base");
//		attributeIndex.addAttribute("ctag");
			
		Paragraph paragraph = new Paragraph(nextParagraphId);
		paragraph.setAttributeIndex(this.attributeIndex);
		this.nextParagraphId = null;
		this.nextParagraph = false;
		Sentence currentSentence = new Sentence();
		Chunk currentChunk = null;

		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					if (currentSentence.getTokenNumber() > 0) {
//						currentSentence.setAttributeIndex(this.attributeIndex);
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
//					currentSentence.setAttributeIndex(this.attributeIndex);
					paragraph.addSentence(currentSentence);
					currentSentence = new Sentence();
				}
			}
			else {
				String[] words = line.trim().split(" ");
				if (words[0].equals("-DOCSTART")) {
					if (words[1].equals("FILE")) {
						if (words.length >= 3)
							this.nextParagraphId = words[2];
						this.nextParagraph = true;
						return paragraph;
					}
					else
						continue;
				}
//				if ((words[0].equals("-DOCSTART")) &&
//					(words[1].equals("FILE"))) {
//					if (words.length >= 3)
//						this.nextParagraphId = words[2];
//					this.nextParagraph = true;
//					return paragraph;
//				}
				else {
					// add token
					try {
						currentSentence.addToken(createToken(words));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					// add or update chunk if I/B tag present
					int last = words.length - 1;
					if (words[last].startsWith("B")) {
						int idx = currentSentence.getTokenNumber() - 1;
						String type = words[last].length() >= 3 ? words[last].substring(2) : "";
						currentChunk = new Chunk(idx, idx, type, currentSentence);
						currentSentence.addChunk(currentChunk);
					}
					else if (words[last].startsWith("I")) {
						if (currentChunk != null) {
							int idx = currentSentence.getTokenNumber() - 1;
							currentChunk.setEnd(idx);
						}
					}
//					currentSentence.addToken(createToken(words));
//					if (words.length > 3) {
//						if (words[3].startsWith("B")) {
//							int idx = currentSentence.getTokenNumber()-1;
//							currentChunk = new Chunk(idx, idx, words[3].substring(2), currentSentence);
//							currentSentence.addChunk(currentChunk);
//						}
//						else if (words[3].startsWith("I")) {
//							if (currentChunk != null) {
//								int idx = currentSentence.getTokenNumber()-1;
//								currentChunk.setEnd(idx);
//							}
//						}
//					}
				}
			}
		}
	}
	
//	private void readParagraphHeader() {
//		while (true) {
//			String line = null;
//			try {
//				if (!ir.ready()) {
//					nextParagraphId = null;
//					return;
//				}
//				line = ir.readLine();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//			
//			String[] words = line.trim().split(" ");
//			if ((words[0].equals("-DOCSTART")) &&
//				(words[1].equals("FILE"))) {
//				nextParagraphId = words[2];
//				return;
//			}
//		}
//	}
	
	private Token createToken(String[] words) throws Exception {
		Token token = new Token();
		if (words.length != this.attributeIndex.getLength() + 1)
			throw new Exception("Invalid number of attributes.");
		for (int i = 0; i < words.length - 1; i++)
			token.setAttributeValue(i, words[i]);
		if (this.attributeIndex != null) {
			String base = this.attributeIndex.getAttributeValue(token, "base");
			String ctag = this.attributeIndex.getAttributeValue(token, "ctag");
			token.addTag(new Tag(base, ctag, false));
		}
//		token.setAttributeValue(0, words[0]);
//		token.addTag(new Tag(words[1], words[2], false));
		return token;
	}
}
