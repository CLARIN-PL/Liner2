package liner2.action;

import java.util.HashMap;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.AbstractDocumentReader;
import liner2.reader.ReaderFactory;
import liner2.structure.AnnotationSet;
import liner2.structure.Document;
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
    	TokenFeatureGenerator gen = null;

    	timer.startTimer("Model loading");
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }        
        timer.stopTimer();

        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
    	
    	if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
    		throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
    	}
    		
    	/* Create all defined chunkers. */
    	ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal().getTypes());
    	ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().getTypes());

    	timer.startTimer("Data reading");
    	Document ps = reader.nextDocument();
    	timer.stopTimer();
    	
    	HashMap<Sentence, AnnotationSet> chunkings = null;
    	while ( ps != null ){
    		timer.startTimer("Feature generation");
            if(gen != null){
                gen.generateFeatures(ps);
            }
            timer.stopTimer();
    		
    		timer.startTimer("Chunking");
    		chunker.prepare(ps);
        	chunkings = chunker.chunk(ps);
        	timer.stopTimer();
        	
        	timer.startTimer("Evaluation", false);
    		timer.addTokens(ps);
        	eval.evaluate(ps.getSentences(), chunkings, ps.getChunkings());
    		evalMuc.evaluate(chunkings, ps.getChunkings());				
    		timer.stopTimer();
            
        	timer.startTimer("Data reading");
        	ps = reader.nextDocument();
        	timer.stopTimer();
    	}
    	    	
		eval.printResults();
		evalMuc.printResults();
		timer.printStats();
	}
}
