package g419.liner2.cli.action;

import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.factory.ChunkerFactory;

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
