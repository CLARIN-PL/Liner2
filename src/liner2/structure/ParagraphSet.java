package liner2.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ParagraphSet {

	TokenAttributeIndex attributeIndex = null;
	ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
		if (paragraph.getAttributeIndex() == null)
			paragraph.setAttributeIndex(this.attributeIndex);
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public ArrayList<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public void setAttributeIndex(TokenAttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Paragraph p : this.paragraphs)
			p.setAttributeIndex(this.attributeIndex);
	}

	public HashMap<Sentence, AnnotationSet> getChunkings() {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, new AnnotationSet(sentence, sentence.getChunks()));
		return chunkings;
	}

	public void addChunks(HashMap<Sentence, AnnotationSet> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.addChunking(chunkings.get(sentence));
	}

	public void setChunks(HashMap<Sentence, AnnotationSet> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.setChunking(chunkings.get(sentence));
	}

	public ArrayList<Sentence> getSentences() {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		for ( Paragraph paragraph : this.paragraphs )
			sentences.addAll(paragraph.getSentences());
		return sentences;
	}
}
