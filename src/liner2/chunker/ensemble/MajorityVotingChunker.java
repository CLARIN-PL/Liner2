package liner2.chunker.ensemble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import liner2.chunker.Chunker;
import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

/**
 * TODO
 * 
 * Klasyfikator głosowania większościowego składa się ze zbioru klasyfikatorów.
 * Każdy z klasyfikatorów wykonuje niezależnie chunkowanie. Wynik zawiera
 * chunki, które zostały rozpoznane przez conajmniej połowę chunkerów.
 * 
 * 
 * @author Maciej Janicki 
 * @author Michał Marcińczuk
 *
 */
public class MajorityVotingChunker extends Chunker {

	ArrayList<Chunker> chunkers;
	
	public MajorityVotingChunker(ArrayList<Chunker> chunkers){
		this.chunkers = chunkers;
	}
	
	public AnnotationSet voting(Sentence sentence, ArrayList<AnnotationSet> chunkings) {
		AnnotationSet resultChunking = new AnnotationSet(sentence);
		
		Hashtable<Annotation, Integer> votes = new Hashtable<Annotation, Integer>();

		for (AnnotationSet chunking : chunkings){
			for (Annotation chunk : chunking.chunkSet()) {
				boolean found = false;
				for (Annotation key : votes.keySet())
					if (chunk.equals(key)) {
						found = true;
						votes.put(key, votes.get(key) + 1);
						break;
					}
				if (!found)
					votes.put(chunk, new Integer(1));
			}
		}
		
		int majority = this.chunkers.size() / 2 + this.chunkers.size() % 2;
		for (Annotation chunk : votes.keySet())
			if (votes.get(chunk) >= majority)
				resultChunking.addChunk(chunk);
		
		return resultChunking;
	}
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(ParagraphSet ps) {
				
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		HashMap<Sentence, ArrayList<AnnotationSet>> sentenceChunkings = new HashMap<Sentence, ArrayList<AnnotationSet>>();

		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				sentenceChunkings.put(sentence, new ArrayList<AnnotationSet>());
		
		for ( Chunker chunker : this.chunkers) {
			HashMap<Sentence, AnnotationSet> chunkingsThis = chunker.chunk(ps);
			for (Sentence sentence : chunkingsThis.keySet())
				sentenceChunkings.get(sentence).add(chunkingsThis.get(sentence));				
		}
		
		for ( Sentence sentence : sentenceChunkings.keySet() )
			chunkings.put(sentence, this.voting(sentence, sentenceChunkings.get(sentence)));
		
		return chunkings;
	}
}
