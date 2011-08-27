package liner2.chunker.ensemble;

import java.util.ArrayList;

import liner2.chunker.Chunker;
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

	
	public MajorityVotingChunker(ArrayList<Chunker> chunkers){
		
	}
	
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		// TODO Auto-generated method stub
		return null;
	}


	
	
}
