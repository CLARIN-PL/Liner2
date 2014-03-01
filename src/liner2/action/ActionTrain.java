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
	 */
	public void run() throws Exception{
		/* Loading chunkers make them train */
        ChunkerFactory.loadChunkers(LinerOptions.getGlobal());

	}
		
}
