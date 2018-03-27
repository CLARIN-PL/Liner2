package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.structure.Document;
import g419.lib.cli.ParameterException;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;
import g419.corpus.ConsolePrinter;
import g419.liner2.core.tools.ProcessingTimer;

/**
 * Measuring processing time.
 */
public class ActionTime extends ActionPipe{

	public ActionTime(){
		super("time");
        this.setDescription("eval mode with processing time logs");
	}
	
	/**
	 * Module entry function.
	 */
	public void run() throws Exception{

        if ( !LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER) ){
            throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
        }

        TokenFeatureGenerator gen = null;
    	ProcessingTimer timer = new ProcessingTimer();

    	timer.startTimer("Model loading", false);
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());    	
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }
    	timer.stopTimer();

    	timer.startTimer("Data reading");
    	AbstractDocumentReader reader = getInputReader();
    	timer.stopTimer();

    	// Setup writing stream
    	AbstractDocumentWriter writer = getOutputWriter();
    	        
    	timer.startTimer("Data reading");
    	Document ps = reader.nextDocument();
    	timer.stopTimer();

    	while ( ps != null ){
    		ConsolePrinter.log("Loaded URI: " + ps.getName());
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
