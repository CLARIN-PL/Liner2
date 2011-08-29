package liner2.structure;

import java.util.HashSet;
import java.util.Iterator;


/**
 * Opakowanie na zbiór chunków przypisanych do jednego zdania.
 *
 */
public class Chunking {

	HashSet<Chunk> chunks = new HashSet<Chunk>();
	Sentence sentence = null;

	public Chunking(Sentence sentence){
		this.sentence = sentence;
	}
	
	public void addChunk(Chunk chunk){
		this.chunks.add(chunk);
	}
	
	public HashSet<Chunk> chunkSet(){
		return chunks;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public boolean contains(Chunk chunk) {
		return this.chunks.contains(chunk);
	}
	
	public void union(Chunking foreignChunking) {
		if (foreignChunking.getSentence() == this.sentence) {
			HashSet<Chunk> foreignChunks = foreignChunking.chunkSet();
			Iterator<Chunk> i_fc = foreignChunks.iterator();
			while (i_fc.hasNext())
				this.chunks.add(i_fc.next());
		}
	}
}
