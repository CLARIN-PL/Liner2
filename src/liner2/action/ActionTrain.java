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

//        StreamReader reader = ReaderFactory.getGlobal().getStreamReader(
//        	LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
//        	LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
//		ParagraphSet ps = reader.readParagraphSet();
		

		/* Loading chunkers make them train */
        ChunkerFactory.loadChunkers(LinerOptions.getGlobal().chunkersDescriptions);

	}
		
}
