package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.chunker.HeuristicChunker;
import liner2.chunker.Chunker;

import liner2.tools.ParameterException;

import liner2.Main;

/*
 * @author Maciej Janicki
 */

public class ChunkerFactoryItemHeuristic extends ChunkerFactoryItem {

	public ChunkerFactoryItemHeuristic() {
		super("heuristic(:(.*))?");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
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
