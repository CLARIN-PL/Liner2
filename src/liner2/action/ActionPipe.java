package liner2.action;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

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
public class ActionPipe extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
	
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}		
	
        StreamReader reader = ReaderFactory.get().getStreamReader(
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
        
		ParagraphSet ps = reader.readParagraphSet();

        if (!LinerOptions.getGlobal().features.isEmpty()){
            TokenFeatureGenerator gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
            gen.generateFeatures(ps);
        }
		
		/* Create all defined chunkers. */
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal().chunkersDescriptions);
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

		chunker.chunkInPlace(ps);
			
		StreamWriter writer = WriterFactory.get().getStreamWriter(
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE),
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT));
		writer.writeParagraphSet(ps);
	}
		
}
