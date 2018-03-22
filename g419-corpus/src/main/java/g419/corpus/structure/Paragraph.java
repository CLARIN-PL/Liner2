package g419.corpus.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Paragraph extends IdentifiableElement {

	private TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	private HashMap<String,String> chunkMetaData = new HashMap<String, String>();
	
	private ArrayList<Sentence> sentences = new ArrayList<Sentence>(); 
	
	public Paragraph(String id) {
		this.id = id;
	}
	
	public void addSentence(Sentence sentence) {
		sentences.add(sentence);
		if (sentence.getAttributeIndex() == null)
			sentence.setAttributeIndex(this.attributeIndex);
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}

	public ArrayList<Sentence> getSentences() {
		return this.sentences;
	}
	
	public void setAttributeIndex(TokenAttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Sentence s : this.sentences)
			s.setAttributeIndex(attributeIndex);
	}
	
	public void setChunkMetaData(HashMap<String,String> chunkMetaData){
		this.chunkMetaData = chunkMetaData;
	}
	
	public Set<String> getKeysChunkMetaData(){
		return this.chunkMetaData.keySet();
	}
	
	public String getChunkMetaData(String key){
		return this.chunkMetaData.get(key);
	}

    public Paragraph clone(){
        Paragraph copy = new Paragraph(id);
        copy.chunkMetaData = new HashMap<String, String>(chunkMetaData);
        copy.attributeIndex = attributeIndex.clone();
        for(Sentence s: sentences){
            copy.addSentence(s.clone());
        }
                return copy;
    }

    public int numSentences(){
        return sentences.size();
    }

	public void setDocument(Document document) {
		for(Sentence sentence : sentences) sentence.setDocument(document);
	}
}
