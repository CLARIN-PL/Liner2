package liner2.action;

import liner2.chunker.factory.ChunkerFactory;
import liner2.LinerOptions;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki
 *
 */
public class ActionPipe extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
        
        ChunkerFactory.get().createChunkers(LinerOptions.get().chunkersDescription);

//        for (Chunker chunker : chunkers.values()){
//        	System.out.println(chunker);
//        	chunker.train(ps);
//        }
	}
		
}
