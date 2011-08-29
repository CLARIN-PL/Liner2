package liner2.structure;

import java.util.ArrayList;

public class Paragraph {

	private String id = null;
	private AttributeIndex attributeIndex = new AttributeIndex();
	
	private ArrayList<Sentence> sentences = new ArrayList<Sentence>(); 
	
	public Paragraph(String id) {
		this.id = id;
	}
	
	public void addSentence(Sentence sentence) {
		sentences.add(sentence);
		if (sentence.getAttributeIndex() == null)
			sentence.setAttributeIndex(this.attributeIndex);
	}
	
	public AttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public String getId() {
		return this.id;
	}
	
	public ArrayList<Sentence> getSentences() {
		return this.sentences;
	}
	
	public void setAttributeIndex(AttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Sentence s : this.sentences)
			s.setAttributeIndex(attributeIndex);
	}
}
