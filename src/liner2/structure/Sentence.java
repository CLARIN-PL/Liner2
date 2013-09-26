package liner2.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashSet;


/**
 * Reprezentuje zdanie jako sekwencję tokenów i zbiór anotacji.
 * @author czuk
 *
 */
public class Sentence {
	
	/* Indeks nazw atrybutów */
	TokenAttributeIndex attributeIndex = null;
	
	/* Sekwencja tokenów wchodzących w skład zdania */
	ArrayList<Token> tokens = new ArrayList<Token>();
	
	/* Zbiór anotacji */
	LinkedHashSet<Annotation> chunks = new LinkedHashSet<Annotation>();
	
	/* Identyfikator zdania */
	String id = null;
	
	public Sentence()	{}
	
	public void addChunk(Annotation chunk) {
		chunks.add(chunk);
	}
	
	public void addChunking(AnnotationSet chunking) {
		if ( chunking != null)
			for (Annotation chunk : chunking.chunkSet())
				addChunk(chunk);
	}
	
	public void addToken(Token token) {
		tokens.add(token);
	}
	
	public String getId(){
		return this.id;
	}
	
	/*
	 * Zwraca chunk dla podanego indeksu tokenu.
	 * TODO zmienić parametr na token?
	 */
	public Annotation getChunkAt(int idx) {
		Annotation returning = null;
		Iterator<Annotation> i_chunk = chunks.iterator();
		while (i_chunk.hasNext()) {
			Annotation currentChunk = i_chunk.next();
			if ((currentChunk.getBegin() <= idx) &&
				(currentChunk.getEnd() >= idx)) {
				returning = currentChunk;
				break;
			}
		}
		return returning;
	}
	
	public HashSet<Annotation> getChunks() {
		return this.chunks;
	}
	
	public int getAttributeIndexLength() {
		return this.attributeIndex.getLength();
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	/*
	 * Zwraca ilość tokenów.
	 */
	public int getTokenNumber() {
		return tokens.size();
	}
	
	public ArrayList<Token> getTokens() {
		return tokens;
	}
	
	public void setAttributeIndex(TokenAttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
	}

	public void setChunking(AnnotationSet chunking) {
		this.chunks = (LinkedHashSet<Annotation>) chunking.chunkSet();
	}
	
	public void setId(String id){
		this.id = id;
	}

    public String annotationsToString(){
        StringBuilder output = new StringBuilder();
        for(Annotation chunk: chunks)
            output.append(chunk.getType()+" | "+chunk.getText()+"\n");
        return output.toString();
    }

}
