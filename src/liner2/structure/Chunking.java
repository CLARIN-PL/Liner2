package liner2.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import liner2.filter.Filter;

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
	
	public void filter(ArrayList<Filter> filters) {
		HashSet<Chunk> filteredChunks = new HashSet<Chunk>();
		for (Chunk chunk : this.chunks) {
			Chunk filteredChunk = Filter.filter(chunk, filters);
			if (filteredChunk != null)
				filteredChunks.add(filteredChunk);
		}
		this.chunks = filteredChunks;
	}
	
	public void union(Chunking foreignChunking) {
		if (foreignChunking.getSentence() == this.sentence) {
			HashSet<Chunk> foreignChunks = foreignChunking.chunkSet();
			Iterator<Chunk> i_fc = foreignChunks.iterator();
			while (i_fc.hasNext()) {
				Chunk foreignChunk = i_fc.next();
				boolean found = false;
				for (Chunk chunk : this.chunks)
					if (chunk.equals(foreignChunk)) {
						found = true;
						break;
					}
				if (!found)
					addChunk(foreignChunk);
			}
		}
	}
}
