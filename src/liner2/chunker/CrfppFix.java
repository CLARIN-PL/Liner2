package liner2.chunker;

import java.util.ArrayList;
import java.util.HashMap;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

public class CrfppFix extends Chunker {
	
	private Chunker chunker = null;

	
    public CrfppFix(Chunker baseChunker) {
		this.chunker = baseChunker;
    }

    /**
     * Reads output from the external CRF tagger. 
     * Transforms the result from IOB format into a list of annotations.
     * @param cSeq --- text to tag
     * @return chunking with annotations
     */
	private synchronized void fixSentenceChunking(Sentence sentence, Chunking chunking){

		AttributeIndex ai = sentence.getAttributeIndex();
		
		ArrayList<Token> tokens = sentence.getTokens();
		ArrayList<Chunk> newChunks = new ArrayList<Chunk>();
		
		for (Chunk chunk : chunking.chunkSet()){
			
			int end = chunk.getEnd();
			int start = chunk.getBegin();
			int len = end - start + 1;
			String w = tokens.get(end).getFirstValue();
			
			if ( w.equals(":") || w.equals(",")){
				chunk.setEnd(end-1);
			}
			else if ( len >2
					&& ai.getAttributeValue(tokens.get(end), "person_last_nam").equals("B")
					&& ai.getAttributeValue(tokens.get(end-1), "person_first_nam").equals("B") 
					&& ai.getAttributeValue(tokens.get(end-2), "person_first_nam").equals("O")
					&& ai.getAttributeValue(tokens.get(end-2), "pattern").equals("UPPER_INIT")){
				chunk.setBegin(end-1);				
				newChunks.add(new Chunk(start, end-2, "NAM", sentence));
			}											
		}
		
		for (Chunk chunk : newChunks)
			chunking.addChunk(chunk);		
	}

	@Override
	public HashMap<Sentence, Chunking> chunk(ParagraphSet ps) {
		/* Get base chunking for every sentence. */
		HashMap<Sentence, Chunking> chunkings = this.chunker.chunk(ps);

		for (Sentence sentence : chunkings.keySet())
				this.fixSentenceChunking(sentence, chunkings.get(sentence));
		return chunkings;
	}
	
}
