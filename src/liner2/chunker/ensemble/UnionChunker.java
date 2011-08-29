package liner2.chunker.ensemble;

import java.util.ArrayList;

import liner2.chunker.Chunker;
import liner2.structure.Chunking;
import liner2.structure.Sentence;

/**
 * TODO
 * 
 * Chunker stanowi sumę podanych chunkerów. Wynikiem jest zbiór chunków
 * rozpoznanych przez kolejne chunki z usunięciem duplikatów.
 * 
 * @author Maciej Janicki 
 * @author Michał Marcińczuk
 *
 */
public class UnionChunker extends Chunker {

	private ArrayList<Chunker> chunkers;

	public UnionChunker(ArrayList<Chunker> chunkers){
		this.chunkers = chunkers;
	}
		
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		Chunking chunking = new Chunking(sentence);
		for (Chunker chunker : this.chunkers)
			chunking.union(chunker.chunkSentence(sentence));
		return chunking;
	}
	
}
