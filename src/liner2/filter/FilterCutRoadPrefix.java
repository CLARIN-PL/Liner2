package liner2.filter;

import java.util.ArrayList;

import liner2.structure.Chunk;
import liner2.structure.Token;

public class FilterCutRoadPrefix extends Filter {

	public FilterCutRoadPrefix(){
		this.appliesTo.add("ROAD_NAM");
	}
	
	@Override
	public String getDescription() {
		return "Cut of road prefix 'ul.'";
	}

	@Override
	public Chunk pass(Chunk chunk, CharSequence charSeq) {
		ArrayList<Token> tokens = chunk.getSentence().getTokens();
		int begin = chunk.getBegin();
		int end = chunk.getEnd();
		if (end - begin > 2)
			if ((tokens.get(begin).getFirstValue().equals("ul")) &&
				(tokens.get(begin + 1).getFirstValue().equals(".")))
				return new Chunk(begin + 2, end, chunk.getType(), chunk.getSentence());
		return chunk;
	}

}
