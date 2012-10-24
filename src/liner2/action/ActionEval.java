package liner2.action;

import java.util.HashMap;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.reader.FeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.Chunking;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.tools.ChunkerEvaluator;
import liner2.tools.ChunkerEvaluatorMuc;
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
		
	public void run() throws Exception {
		long timeTotalStart = System.nanoTime();
		if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
		
        StreamReader reader = ReaderFactory.get().getStreamReader(
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE),
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
    		ParagraphSet ps = reader.readParagraphSet();

    	if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
    		throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
    	}
    		
    	/* Create all defined chunkers. */
    	ChunkerEvaluator eval = new ChunkerEvaluator();
    	ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc();

    	eval.startTimer();
    	ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
    	Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
		chunker.prepare(ps);
		eval.stopTimer();
		
    	HashMap<Sentence, Chunking> chunkingsRef = ps.getChunkings();
    	HashMap<Sentence, Chunking> chunkings = chunker.chunk(ps); 
    	    	
    	eval.evaluate(chunkings, chunkingsRef);
		eval.printResults();

		evalMuc.evaluate(chunkings, chunkingsRef);				
		evalMuc.printResults();
		
		long timeTotal = System.nanoTime() - timeTotalStart;
		eval.tokensTime = timeTotal - eval.getTime() - FeatureGenerator.initTime;
		double timeTotalSeconds = (double)timeTotal / 1000000000;
		System.out.println(String.format("Liner2 total time: %.4f s", timeTotalSeconds));
	}
}
