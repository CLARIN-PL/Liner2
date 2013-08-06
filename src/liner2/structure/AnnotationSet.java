package liner2.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import liner2.filter.Filter;

/**
 * Opakowanie na zbiór chunków przypisanych do jednego zdania.
 *
 */
public class AnnotationSet {

	HashSet<Annotation> chunks = new HashSet<Annotation>();
	Sentence sentence = null;

	public AnnotationSet(Sentence sentence, HashSet<Annotation> chunks){
		this.chunks = chunks;
		this.sentence = sentence;
	}
	
	public AnnotationSet(Sentence sentence){
		this.sentence = sentence;
	}
	
	public void addChunk(Annotation chunk){
		this.chunks.add(chunk);
	}
	
	public void removeChunk(Annotation chunk) {
		this.chunks.remove(chunk);
	}
	
	public HashSet<Annotation> chunkSet(){
		return chunks;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public boolean contains(Annotation chunk) {
		return this.chunks.contains(chunk);
	}
	
	public void filter(ArrayList<Filter> filters) {
		HashSet<Annotation> filteredChunks = new HashSet<Annotation>();
		for (Annotation chunk : this.chunks) {
			Annotation filteredChunk = Filter.filter(chunk, filters);
			if (filteredChunk != null)
				filteredChunks.add(filteredChunk);
		}
		this.chunks = filteredChunks;
	}
	
	public void union(AnnotationSet foreignChunking) {
		if (foreignChunking.getSentence() == this.sentence) {
			HashSet<Annotation> foreignChunks = foreignChunking.chunkSet();
			Iterator<Annotation> i_fc = foreignChunks.iterator();
			while (i_fc.hasNext()) {
				Annotation foreignChunk = i_fc.next();
				boolean found = false;
				HashSet<Annotation> chunksToRemove = new HashSet<Annotation>();
				for (Annotation chunk : this.chunks)
					if (chunk.equals(foreignChunk)) {
						found = true;
						break;
					}
					else if (chunk.getType().equals(foreignChunk.getType())) {
						int cb = chunk.getBegin();
						int ce = chunk.getEnd();
						int fb = foreignChunk.getBegin();
						int fe = foreignChunk.getEnd();
						if ((cb <= fb) && (ce >= fe)) {
							found = true;
							break;
						}
						else if ((cb >= fb) && (ce <= fe)) {
							chunksToRemove.add(chunk);
						}
                        else if ((cb >= fb && cb <= fe) || (ce >= fb && ce <=fe) ){
                            found = true;
                            break;
                        }
					}
				for (Annotation chunkToRemove : chunksToRemove)
					removeChunk(chunkToRemove);
				if (!found)
					addChunk(foreignChunk);
			}
		}
	}
}
