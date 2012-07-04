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

import java.io.*;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionBatchFile extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
	
                if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
		
		if ( !LinerOptions.isOption(LinerOptions.OPTION_IS) ){
                        throw new ParameterException("Parameter --is <file_with_input_files_list> not set");
                }
		System.out.println(LinerOptions.getOption(LinerOptions.OPTION_USE));
		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
		Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));

		File file = null;
  		FileReader freader = null;
  		LineNumberReader lnreader = null;
		System.out.println("1");
  		try{
  			file = new File(LinerOptions.getOption(LinerOptions.OPTION_IS));
  			freader = new FileReader(file);
  			lnreader = new LineNumberReader(freader);
  			String line = "";
			System.out.println("2");
			while ((line = lnreader.readLine()) != null){
				StreamReader reader = ReaderFactory.get().getStreamReader(
                        		line,
                        		LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
                		ParagraphSet ps = reader.readParagraphSet();
				System.out.println("3");
				for (Paragraph p : ps.getParagraphs())
                        		for (Sentence s : p.getSentences())
                                		chunker.chunkSentenceInPlace(s);
				System.out.println("4");
                		StreamWriter writer = WriterFactory.get().getStreamWriter(
                        		line+"."+LinerOptions.getOption(LinerOptions.OPTION_OUTPUT_FORMAT),
                        		LinerOptions.getOption(LinerOptions.OPTION_OUTPUT_FORMAT));
                		writer.writeParagraphSet(ps);
				System.out.println("5");
			}
		}
		finally{
  			freader.close();
  			lnreader.close();
  		}
		System.out.println("6");
		
		/* Create all defined chunkers. */
		

	}
		
}
