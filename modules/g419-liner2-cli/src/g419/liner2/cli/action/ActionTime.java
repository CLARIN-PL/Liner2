package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.ParameterException;
import g419.liner2.api.tools.ProcessingTimer;

/**
 * Measuring processing time.
 */
public class ActionTime extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
        LinerOptions.getGlobal().setDefaultDataFormats("ccl", "ccl");
	
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}		
        
        TokenFeatureGenerator gen = null;
    	ProcessingTimer timer = new ProcessingTimer();

    	timer.startTimer("Model loading", false);
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());    	
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }
    	timer.stopTimer();

    	timer.startTimer("Data reading");
    	AbstractDocumentReader reader = LinerOptions.getGlobal().getInputReader();        
    	timer.stopTimer();

    	// Setup writing stream
    	AbstractDocumentWriter writer = LinerOptions.getGlobal().getOutputWriter();
    	        
    	timer.startTimer("Data reading");
    	Document ps = reader.nextDocument();
    	timer.stopTimer();

    	while ( ps != null ){
    		Logger.log("Loaded URI: " + ps.getName());
    		timer.addTokens(ps);
    		
    		// Generate features
        	timer.startTimer("Feature generation");
        	if (gen != null){
                gen.generateFeatures(ps);    		
        	}
        	timer.stopTimer();
    		
        	timer.startTimer("Recognition");
    		chunker.chunkInPlace(ps);
        	timer.stopTimer();
        	
        	timer.startTimer("Data writing");
    		writer.writeDocument(ps);
        	timer.stopTimer();

    		// Read next document from the stream
        	timer.startTimer("Data reading");
        	ps = reader.nextDocument();
        	timer.stopTimer();
    	}
    	reader.close();
    	writer.close();
		timer.printStats();    	
	}
			
}
