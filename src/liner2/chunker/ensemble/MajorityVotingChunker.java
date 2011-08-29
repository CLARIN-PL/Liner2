package liner2.chunker.ensemble;

import java.util.ArrayList;
import java.util.Hashtable;

import liner2.chunker.Chunker;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
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
	
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		Chunking resultChunking = new Chunking(sentence);
		
		Hashtable<Chunk, Integer> votes = new Hashtable<Chunk, Integer>();
		for (Chunker chunker : this.chunkers) {
			Chunking chunking = chunker.chunkSentence(sentence);
			for (Chunk chunk : chunking.chunkSet()) {
				boolean found = false;
				for (Chunk key : votes.keySet())
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
		for (Chunk chunk : votes.keySet())
			if (votes.get(chunk) >= majority)
				resultChunking.addChunk(chunk);
		
		return resultChunking;
	}
}
