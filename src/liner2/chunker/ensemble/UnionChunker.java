package liner2.chunker.ensemble;

import java.util.ArrayList;
import java.util.HashMap;

import liner2.chunker.Chunker;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
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
		
	public HashMap<Sentence, Chunking> chunk(ParagraphSet ps) {
		HashMap<Sentence, Chunking> chunkings = new HashMap<Sentence, Chunking>();
		
		for (Paragraph p : ps.getParagraphs())
			for (Sentence sentence : p.getSentences())
				chunkings.put(sentence, new Chunking(sentence));
		
		for ( Chunker chunker : this.chunkers){
			HashMap<Sentence, Chunking> chunkingThis = chunker.chunk(ps);
			for (Sentence sentence : chunkingThis.keySet())
				chunkings.get(sentence).union(chunkingThis.get(sentence));
				
		}
		
		return chunkings;
	}	
	
	
}
