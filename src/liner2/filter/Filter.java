package liner2.filter;

import java.util.ArrayList;

import liner2.structure.Chunk;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.Main;

public abstract class Filter {

	// List of annotation types to which the filter can be applied
	protected ArrayList<String> appliesTo = new ArrayList<String>(); 
	
	/**
	 * Check if chunk passes a filter condition.
	 * @param chunk
	 * @param charSeq
	 * @return
	 */
	protected abstract Chunk pass(Chunk chunk, CharSequence charSeq);
	
	public abstract String getDescription();
	
	public Chunk run(Chunk chunk, CharSequence charSeq)
	{
		if (appliesTo.contains(chunk.getType()))
			return this.pass(chunk, charSeq);
		else
			return chunk;
	}
			
	/**
	 * Pass chunk through set of filters.
	 * @param chunk
	 * @param sentence
	 * @param filters
	 * @return
	 */
	static public Chunk filter(Chunk chunk, ArrayList<Filter> filters) {
    	StringBuilder sb = new StringBuilder();
    	ArrayList<Token> tokens = chunk.getSentence().getTokens();
    	for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++) {
    		Token token = tokens.get(i);
    		sb.append(token.getFirstValue() + (token.getNoSpaceAfter() ? "" : " "));
    	}
    	return Filter.filter(chunk, sb.toString().trim(), filters);
	}
	
	static private Chunk filter(Chunk chunk, CharSequence cSeq, ArrayList<Filter> filters) {
		Chunk chunkMod = chunk;        	
    	for (Filter filter : filters){    		
    		Chunk chunkFiltered = filter.run(chunkMod, cSeq);

    		if (chunkFiltered == null)
    			return null;
    		
    		chunkMod = chunkFiltered;        		
    	}
    	return chunkMod;
	}	
}
