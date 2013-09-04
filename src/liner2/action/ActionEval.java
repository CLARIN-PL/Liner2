package liner2.action;

import java.util.HashMap;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.AnnotationSet;
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
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal().chunkersDescriptions);
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
		timer.stopTimer();

        StreamReader reader = ReaderFactory.get().getStreamReader(
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
    	
    	timer.startTimer("Data reading");
    	ParagraphSet ps = reader.readParagraphSet();

        if (!LinerOptions.getGlobal().features.isEmpty()){
            TokenFeatureGenerator gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
            gen.generateFeatures(ps);
        }

		chunker.prepare(ps);
    	timer.stopTimer();

    	if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
    		throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
    	}
    		
    	/* Create all defined chunkers. */
    	ChunkerEvaluator eval = new ChunkerEvaluator();
    	ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc();

		timer.startTimer("Chunking");
    	HashMap<Sentence, AnnotationSet> chunkingsRef = ps.getChunkings();
    	HashMap<Sentence, AnnotationSet> chunkings = chunker.chunk(ps);
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
