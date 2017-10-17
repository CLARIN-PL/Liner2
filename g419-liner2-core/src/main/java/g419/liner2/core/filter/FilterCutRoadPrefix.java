package g419.liner2.core.filter;

import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;


public class FilterCutRoadPrefix extends Filter {

	public FilterCutRoadPrefix(){
		this.appliesTo.add("ROAD_NAM");
	}
	
	@Override
	public String getDescription() {
		return "Cut of road prefix 'ul.'";
	}

	@Override
	public Annotation pass(Annotation chunk, CharSequence charSeq) {
		List<Token> tokens = chunk.getSentence().getTokens();
		int begin = chunk.getBegin();
		int end = chunk.getEnd();
		if (end - begin > 2)
			if ((tokens.get(begin).getOrth().equals("ul")) &&
				(tokens.get(begin + 1).getOrth().equals(".")))
				return new Annotation(begin + 2, end, chunk.getType(), chunk.getSentence());
		return chunk;
	}

}
