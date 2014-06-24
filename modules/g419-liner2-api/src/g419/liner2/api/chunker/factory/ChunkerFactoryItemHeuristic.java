package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.HeuristicChunker;
import g419.liner2.api.tools.ParameterException;

import java.util.regex.Matcher;




/*
 * @author Maciej Janicki
 */

public class ChunkerFactoryItemHeuristic extends ChunkerFactoryItem {

	public ChunkerFactoryItemHeuristic() {
		super("heuristic(:(.*))?");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcherHeuristic = this.pattern.matcher(description);
		if (matcherHeuristic.find()) {
			HeuristicChunker chunker = null;
			if (matcherHeuristic.group(2) != null) {
				try {
					chunker = new HeuristicChunker(matcherHeuristic.group(2).split(","));
				} catch (ParameterException ex) {
					ex.printStackTrace();
				}
			}
			else
				 chunker = new HeuristicChunker();
			return chunker;
		}
		return null;
	}

}
