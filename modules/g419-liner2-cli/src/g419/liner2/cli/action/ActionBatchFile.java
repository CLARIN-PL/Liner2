package g419.liner2.cli.action;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.tools.ParameterException;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;


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
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

		File file = null;
  		FileReader freader = null;
  		LineNumberReader lnreader = null;
		int i=0;
  		try{
  			file = new File(LinerOptions.getGlobal().getOption(LinerOptions.OPTION_IS));
  			freader = new FileReader(file);
  			lnreader = new LineNumberReader(freader);
  			String line = "";
			while ((line = lnreader.readLine()) != null){
				i++;
				System.out.println(i+": "+line);
				AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
                		line, LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
                Document ps = reader.nextDocument();

                chunker.chunkInPlace(ps);

                String output_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
                String output_file = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE);
                AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        		writer.writeDocument(ps);

				reader.close();
				writer.close();
				ps = null;	
			}
		}
		finally{
  			freader.close();
  			lnreader.close();
  		}
		
	}
		
}
