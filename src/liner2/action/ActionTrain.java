package liner2.action;

import liner2.LinerOptions;
import liner2.chunker.factory.ChunkerFactory;

/**
 * Train chunkers.
 * @author Michał Marcińczuk
 *
 */
public class ActionTrain extends Action{

	/**
	 * Module entry function.
	 * 
	 * Loads annotation recognizers.
	 */
	public void run() throws Exception{
        ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
	}
		
}
