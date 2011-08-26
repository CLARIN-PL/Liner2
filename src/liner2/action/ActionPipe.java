package liner2.action;

import java.util.Hashtable;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

import liner2.LinerOptions;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki
 *
 */
public class ActionPipe extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
        
        StreamReader reader = ReaderFactory.get().getStreamReader(
			LinerOptions.get().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.get().getOption(LinerOptions.OPTION_INPUT_FORMAT));
		ParagraphSet ps = reader.readParagraphSet();
        
        Hashtable<String, Chunker> chunkers = ChunkerFactory.get().createChunkers(LinerOptions.get().chunkersDescription);

		for (Paragraph p : ps.getParagraphs())
			for (Sentence s : p.getSentences()) {
				for (Chunker chunker : chunkers.values()) {
					// TODO zmienić wynik Chunker.chunkSentence() z Chunking na void
					// zapisywać ochunkowanie razem ze zdaniem w obiekcie Sentence
//					chunker.chunkSentence(s);
				}
			}
			
		StreamWriter writer = WriterFactory.get().getStreamWriter(
			LinerOptions.get().getOption(LinerOptions.OPTION_OUTPUT_FILE),
			LinerOptions.get().getOption(LinerOptions.OPTION_OUTPUT_FORMAT));
		writer.writeParagraphSet(ps);
	}
		
}
