package liner2.chunker.ensemble;

import java.util.ArrayList;
import java.util.HashMap;

import liner2.structure.Chunk;
import liner2.structure.Chunking;

public class MajorityVoting {

	/**
	 * Perform major
	 * @param chunkings
	 * @return
	 */
	public static Chunking run(ArrayList<Chunking> chunkings){

		ArrayList<Double> weights = new ArrayList<Double>();
		for (int i=0; i<chunkings.size(); i++)
			weights.add(1.0);
		
		return runWeighted(chunkings, weights);
		
//		if (chunkings.size() == 0)
//			return null;
//		
//		HashMap<String, Integer> voting = new HashMap<String, Integer>();
//		HashMap<String, Chunk> chunks = new HashMap<String, Chunk>();
//		
//		for ( Chunking chunking : chunkings )
//			for ( Chunk chunk : chunking.chunkSet() ){
//				String chunkStr = ChunkTools.chunkToString(chunk);
//				if (voting.containsKey(chunkStr))
//				{
//					Integer in = voting.get(chunkStr) + 1;
//					voting.remove(chunkStr);
//					voting.put(chunkStr, in);
//				}
//				else
//				{
//					voting.put(chunkStr, 1);
//					chunks.put(chunkStr, chunk);
//				}
//			}
//		
//		double threshold = (double)chunkings.size()/2.0;
//		
//		ChunkingImpl chunking = new ChunkingImpl(chunkings.get(0).charSequence());
//		for ( String chunkStr : chunks.keySet() ){
//			int score = voting.get(chunkStr);
//			Chunk chunk = chunks.get(chunkStr);
//			if ( score > threshold )
//				chunking.add(chunk);
//		}
//		
//		return chunking;
	}
	
	public static Chunking runWeighted(ArrayList<Chunking> chunkings, ArrayList<Double> weights){
		
		if (chunkings.size() == 0)
			return null;
		
		HashMap<String, Double> voting = new HashMap<String, Double>();
		HashMap<String, Chunk> chunks = new HashMap<String, Chunk>();
		
		int index = 0;
		for ( Chunking chunking : chunkings ){
			for ( Chunk chunk : chunking.chunkSet() ){
				String chunkStr = chunk.getText();
				Double score = weights.get(index);
				if (voting.containsKey(chunkStr))
				{
					score += voting.get(chunkStr);
					voting.remove(chunkStr);
				}
				else
					chunks.put(chunkStr, chunk);
					
				voting.put(chunkStr, score);
			}
			index++;
		}
		
		double threshold = (double)chunkings.size()/2.0;
		
		Chunking chunking = new Chunking(chunkings.get(0).getSentence());
		for ( String chunkStr : chunks.keySet() ){
			double score = voting.get(chunkStr);
			Chunk chunk = chunks.get(chunkStr);
			if ( score > threshold )
				chunking.addChunk(chunk);
		}
		
		return chunking;
	}
}
