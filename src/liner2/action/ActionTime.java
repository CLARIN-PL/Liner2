package liner2.action;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.tools.ProcessingTimer;
import liner2.tools.Template;
import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.structure.ParagraphSet;
import liner2.tools.ParameterException;

import liner2.LinerOptions;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionTime extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
	
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}		

    	ProcessingTimer timer = new ProcessingTimer();

    	timer.startTimer("Model loading");
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
    	timer.stopTimer();

    	timer.startTimer("Data reading");
    	StreamReader reader = ReaderFactory.get().getStreamReader(
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));        
		ParagraphSet ps = reader.readParagraphSet();
    	timer.stopTimer();

    	timer.startTimer("Feature generation");
        if (!LinerOptions.getGlobal().features.isEmpty()){
            TokenFeatureGenerator gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
            gen.generateFeatures(ps);
        }
    	timer.stopTimer();
		
    	timer.startTimer("Recognition");
		chunker.chunkInPlace(ps);
    	timer.stopTimer();

    	timer.startTimer("Data writing");
        String output_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
        String output_file = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE);
        StreamWriter writer;
        if (output_format.equals("arff")){
            Template arff_template = LinerOptions.getGlobal().getArffTemplate();
            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        }
		writer.writeParagraphSet(ps);
    	timer.stopTimer();
    	
		timer.countTokens(ps);
		timer.printStats();    	
	}
		
}
