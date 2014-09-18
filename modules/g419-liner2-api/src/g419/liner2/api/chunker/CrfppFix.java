package g419.liner2.api.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.ArrayList;
import java.util.HashMap;


public class CrfppFix extends Chunker {
	
	private Chunker chunker = null;

	
    public CrfppFix(Chunker baseChunker) {
		this.chunker = baseChunker;
    }
    
    /**
     * 
     */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		/* Get base chunking for every sentence. */
		HashMap<Sentence, AnnotationSet> chunkings = this.chunker.chunk(ps);

		for (Sentence sentence : chunkings.keySet()){
		    sentence.setAnnotations(fixSentenceChunking(sentence, chunkings.get(sentence)));
            sentence.setAnnotations(fixParanthesis(sentence, chunkings.get(sentence)));
            sentence.setAnnotations(mergeSubstNomChunks(sentence, chunkings.get(sentence)));
		}
		
		return chunkings;
	}

	/**
	 * Merge two annotations both of length one, subst and nom, one after another.
	 * @param sentence
	 * @param chunking
	 */
    private AnnotationSet mergeSubstNomChunks(Sentence sentence, AnnotationSet chunking) {
        AnnotationSet fixedChunking = new AnnotationSet(sentence);
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
                    fixedChunking.addChunk(c1);
				}
			}
		}
        return fixedChunking;
	}
    
	/**
	 * @param sentence
	 * @param chunking
	 */
    private AnnotationSet fixParanthesis(Sentence sentence, AnnotationSet chunking) {

        AnnotationSet fixedChunking = new AnnotationSet(sentence);
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
            fixedChunking.addChunk(chunk);
    	}
        return fixedChunking;
	}    

	/**
     * Reads output from the external CRF tagger. 
     * Transforms the result from IOB format into a list of annotations.
     * @param cSeq --- text to tag
     * @return chunking with annotations
     */
	private synchronized AnnotationSet fixSentenceChunking(Sentence sentence, AnnotationSet chunking){

		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		ArrayList<Token> tokens = sentence.getTokens();
        AnnotationSet fixedChunking = new AnnotationSet(sentence);
		
		for (Annotation chunk : chunking.chunkSet()){
			
			int end = chunk.getEnd();
			int start = chunk.getBegin();
			int len = end - start + 1;
//			String w = tokens.getGlobal(end).getOrth();
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
                fixedChunking.addChunk(chunk);
                fixedChunking.addChunk(new Annotation(start, end-2, "NAM", sentence));
            }
            else{
                fixedChunking.addChunk(chunk);
            }
		}
		
		return fixedChunking;
	}

	
}
