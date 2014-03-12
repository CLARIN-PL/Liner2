package liner2.action;

import liner2.LinerOptions;
import liner2.Main;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.AbstractDocumentReader;
import liner2.structure.Document;
import liner2.tools.ParameterException;
import liner2.tools.ProcessingTimer;
import liner2.writer.AbstractDocumentWriter;

/**
 * Measuring processing time.
 */
public class ActionTime extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
	
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
    		Main.log("Loaded URI: " + ps.getUri());
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
