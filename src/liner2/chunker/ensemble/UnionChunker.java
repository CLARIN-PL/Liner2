package liner2.chunker.ensemble;

import java.util.ArrayList;
import java.util.HashMap;

import liner2.chunker.Chunker;
import liner2.structure.AnnotationSet;
import liner2.structure.Paragraph;
import liner2.structure.Document;
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
		
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		
		for (Paragraph p : ps.getParagraphs())
			for (Sentence sentence : p.getSentences())
				chunkings.put(sentence, new AnnotationSet(sentence));
		
		for ( Chunker chunker : this.chunkers){
			HashMap<Sentence, AnnotationSet> chunkingThis = chunker.chunk(ps);
			for (Sentence sentence : chunkingThis.keySet())
				chunkings.get(sentence).union(chunkingThis.get(sentence));
				
		}
		
		return chunkings;
	}	
	
	
}
