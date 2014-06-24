package g419.liner2.cli.action;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ChunkerEvaluator;
import g419.liner2.api.tools.ChunkerEvaluatorMuc;
import g419.liner2.api.tools.ParameterException;
import g419.liner2.api.tools.ProcessingTimer;

import java.util.HashMap;


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
    	
    	System.out.print("Annotations to evaluate:");
    	for (String annotation : LinerOptions.getGlobal().getTypes())
    		System.out.print(" " + annotation);
    	System.out.println();

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

    		/* Get reference set of annotations */
    		HashMap<Sentence, AnnotationSet> referenceChunks = ps.getChunkings();
    		
    		/* Remove annotation to be evaluated */
    		for (String annotation : LinerOptions.getGlobal().getTypes())
    			ps.removeAnnotations(annotation);	    		
    			    		
    		/* Generate features */    		
    		timer.startTimer("Feature generation");
    		if ( gen != null )
    			gen.generateFeatures(ps);
            timer.stopTimer();
    		
    		timer.startTimer("Chunking");
    		chunker.prepare(ps);
    		try{
    			chunkings = chunker.chunk(ps);
    		}
    		catch(Exception ex){
    			System.err.println("Failed to chunk a sentence in document " + ps.getName());
    			ex.printStackTrace(System.err);
    			chunkings = new HashMap<Sentence, AnnotationSet>();
    		}
        	timer.stopTimer();
        	
        	timer.startTimer("Evaluation", false);
    		timer.addTokens(ps);
        	eval.evaluate(ps.getSentences(), chunkings, referenceChunks);
    		evalMuc.evaluate(chunkings, referenceChunks);				
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
