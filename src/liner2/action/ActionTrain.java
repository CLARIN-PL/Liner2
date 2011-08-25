package liner2.action;

import liner2.LinerOptions;
import liner2.Main;
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

        for (String chunkerDescription : LinerOptions.get().chunkersDescription){
        	Main.log(chunkerDescription);
        	ChunkerFactory.get().createChunker(chunkerDescription);
        	Main.log("TRAINED");
        	Main.log("");
        }
        
	}
		
}
