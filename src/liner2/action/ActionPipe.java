package liner2.action;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
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
        
        StreamReader reader = ReaderFactory.get().getStreamReader(
			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
		ParagraphSet ps = reader.readParagraphSet();
		
		if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
		
		/* Create all defined chunkers. */
		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
		
		Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));

		for (Paragraph p : ps.getParagraphs())
			for (Sentence s : p.getSentences())
				chunker.chunkSentenceInPlace(s);
			
		StreamWriter writer = WriterFactory.get().getStreamWriter(
			LinerOptions.getOption(LinerOptions.OPTION_OUTPUT_FILE),
			LinerOptions.getOption(LinerOptions.OPTION_OUTPUT_FORMAT));
		writer.writeParagraphSet(ps);
	}
		
}
