package liner2.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ParagraphSet {

	AttributeIndex attributeIndex = null;
	ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
		if (paragraph.getAttributeIndex() == null)
			paragraph.setAttributeIndex(this.attributeIndex);
	}
	
	public AttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public ArrayList<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public void setAttributeIndex(AttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Paragraph p : this.paragraphs)
			p.setAttributeIndex(this.attributeIndex);
	}

	public HashMap<Sentence, Chunking> getChunkings() {
		HashMap<Sentence, Chunking> chunkings = new HashMap<Sentence, Chunking>();
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, new Chunking(sentence, sentence.getChunks()));
		return chunkings;
	}

	public void addChunks(HashMap<Sentence, Chunking> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.addChunking(chunkings.get(sentence));
	}

	public void setChunks(HashMap<Sentence, Chunking> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.setChunking(chunkings.get(sentence));
	}
}
