package liner2.action;

import java.util.HashMap;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.features.NerdFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.Chunking;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.tools.ChunkerEvaluator;
import liner2.tools.ChunkerEvaluatorMuc;
import liner2.tools.ParameterException;
import liner2.tools.ProcessingTimer;

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
		if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}

    	ProcessingTimer timer = new ProcessingTimer();

    	timer.startTimer("Chunkers init.");
    	ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
    	Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
		timer.stopTimer();

        StreamReader reader = ReaderFactory.get().getStreamReader(
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE),
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
    	
    	timer.startTimer("Data reading");
    	ParagraphSet ps = reader.readParagraphSet();
		chunker.prepare(ps);
    	timer.stopTimer();

    	if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
    		throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
    	}
    		
    	/* Create all defined chunkers. */
    	ChunkerEvaluator eval = new ChunkerEvaluator();
    	ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc();

		timer.startTimer("Chunking");
    	HashMap<Sentence, Chunking> chunkingsRef = ps.getChunkings();
    	HashMap<Sentence, Chunking> chunkings = chunker.chunk(ps);
    	timer.stopTimer();
    	    	
    	timer.startTimer("Evaluation", false);
    	eval.evaluate(ps.getSentences(), chunkings, chunkingsRef);
		evalMuc.evaluate(chunkings, chunkingsRef);				
		timer.stopTimer();

		eval.printResults();
		evalMuc.printResults();
		timer.countTokens(ps);
		timer.printStats();
	}
}
