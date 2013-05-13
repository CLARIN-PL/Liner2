package liner2.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import liner2.structure.TokenAttributeIndex;
import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

public class CrfppFix extends Chunker {
	
	private Chunker chunker = null;

	
    public CrfppFix(Chunker baseChunker) {
		this.chunker = baseChunker;
    }
    
    /**
     * 
     */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(ParagraphSet ps) {
		/* Get base chunking for every sentence. */
		HashMap<Sentence, AnnotationSet> chunkings = this.chunker.chunk(ps);

		for (Sentence sentence : chunkings.keySet()){
				this.fixSentenceChunking(sentence, chunkings.get(sentence));
				this.fixParanthesis(sentence, chunkings.get(sentence));
				this.mergeSubstNomChunks(sentence, chunkings.get(sentence));				
		}
		
		return chunkings;
	}

	/**
	 * Merge two annotations both of length one, subst and nom, one after another.
	 * @param sentence
	 * @param chunking
	 */
    private void mergeSubstNomChunks(Sentence sentence, AnnotationSet chunking) {
		ArrayList<ArrayList<Annotation>> orderedChunks = new ArrayList<ArrayList<Annotation>>();
		for ( int i=0; i<sentence.getTokens().size(); i++ )
			orderedChunks.add(new ArrayList<Annotation>());
		for ( Annotation chunk : chunking.chunkSet() ){
			orderedChunks.get(chunk.getBegin()).add(chunk);		
		}
		
		int cl = sentence.getAttributeIndex().getIndex("class");
		int ca = sentence.getAttributeIndex().getIndex("case");
		
		for ( int i=0; i+1<orderedChunks.size(); i++){
			if ( orderedChunks.get(i).size() == 1 
					&& orderedChunks.get(i+1).size() == 1 ){
				Annotation c1 = orderedChunks.get(i).get(0);
				Annotation c2 = orderedChunks.get(i+1).get(0);
				Token t1 = sentence.getTokens().get(i);
				Token t2 = sentence.getTokens().get(i+1);
						
				if ( c1.getBegin() == c1.getEnd() 
						&& c2.getBegin() == c2.getEnd() 
						&& c1.getType().equals(c2.getType())
						&& t1.getAttributeValue(cl).equals("subst")
						&& t2.getAttributeValue(cl).equals("subst")
						&& t1.getAttributeValue(ca).equals("nom")
						&& t2.getAttributeValue(ca).equals("nom")){
					orderedChunks.get(i+1).remove(c2);
					chunking.removeChunk(c2);
					c1.addToken(c2.getEnd());
				}
			}
		}		
	}
    
	/**
	 * @param sentence
	 * @param chunking
	 */
    private void fixParanthesis(Sentence sentence, AnnotationSet chunking) {
		
		int index_orth = sentence.getAttributeIndex().getIndex("orth");
		
		for ( Annotation chunk : chunking.chunkSet()){
			int paranthesis = 0;
			if ( chunk.getEnd()+1 < sentence.getTokens().size() ){
				for (int i=chunk.getBegin(); i<=chunk.getEnd(); i++){
					if ( sentence.getTokens().get(i).getAttributeValue(index_orth).equals("&quot;") )
							paranthesis++;
				}
				if ( paranthesis == 1 
						&& sentence.getTokens().get(chunk.getEnd()+1).getAttributeValue(index_orth).equals("&quot;")){
					chunk.addToken(chunk.getEnd()+1);
				}
			}
    	}		
	}    

	/**
     * Reads output from the external CRF tagger. 
     * Transforms the result from IOB format into a list of annotations.
     * @param cSeq --- text to tag
     * @return chunking with annotations
     */
	private synchronized void fixSentenceChunking(Sentence sentence, AnnotationSet chunking){

		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		ArrayList<Token> tokens = sentence.getTokens();
		ArrayList<Annotation> newChunks = new ArrayList<Annotation>();
		
		for (Annotation chunk : chunking.chunkSet()){
			
			int end = chunk.getEnd();
			int start = chunk.getBegin();
			int len = end - start + 1;
//			String w = tokens.get(end).getFirstValue();
//			
//			if ( w.equals(":") || w.equals(",")){
//				chunk.setEnd(end-1);
//			}
//			else 
			if ( len >2
					&& ai.getAttributeValue(tokens.get(end), "person_last_nam").equals("B")
					&& ai.getAttributeValue(tokens.get(end-1), "person_first_nam").equals("B") 
					&& ai.getAttributeValue(tokens.get(end-2), "person_first_nam").equals("O")
					&& ai.getAttributeValue(tokens.get(end-2), "pattern").equals("UPPER_INIT")){
				chunk.replaceTokens(end-1, end);				
				newChunks.add(new Annotation(start, end-2, "NAM", sentence));
			}											
		}
		
		for (Annotation chunk : newChunks)
			chunking.addChunk(chunk);		
	}

	
}
