package liner2.structure;

import java.util.ArrayList;

public class Paragraph {

	private String id = null;
	
	private ArrayList<Sentence> sentences = new ArrayList<Sentence>(); 
	
	public Paragraph(String id) {
		this.id = id;
	}
	
	public void addSentence(Sentence sentence) {
		sentences.add(sentence);
	}
	
	public String getId() {
		return id;
	}
	
	public ArrayList<Sentence> getSentences() {
		return sentences;
	}
}
