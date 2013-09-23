package liner2.action;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.chunker.factory.ChunkerManager;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.tools.Template;
import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.structure.ParagraphSet;
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
				StreamReader reader = ReaderFactory.get().getStreamReader(
                		line, LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
                ParagraphSet ps = reader.readParagraphSet();

                chunker.chunkInPlace(ps);

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
