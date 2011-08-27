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

	public UnionChunker(ArrayList<Chunker> chunkers){
		
	}
		
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
