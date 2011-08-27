package liner2.action;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.tools.ChunkerEvaluator;
import liner2.tools.ParameterException;

/**
 * Evaluate chunker on a specified corpus.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionEval extends Action{

	/**
	 * 
	 */
	public void run() throws Exception
	{
        StreamReader reader = ReaderFactory.get().getStreamReader(
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE),
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
    		ParagraphSet ps = reader.readParagraphSet();

    	if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
    		throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
    	}
    		
    	/* Create all defined chunkers. */
    	ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
    		
    	Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
    		
    	ChunkerEvaluator eval = new ChunkerEvaluator(chunker);
		for (Paragraph p : ps.getParagraphs()){
			eval.evaluate(p);
		}
			
		eval.printResults();
	}
}
