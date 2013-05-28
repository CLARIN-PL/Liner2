package liner2.action;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

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
public class ActionBatch extends Action{

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
		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
		Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));

		File file = null;
  		FileReader freader = null;
  		LineNumberReader lnreader = null;
  		
  		String output_format = LinerOptions.getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
  		String output_ext = output_format;
  		if ( output_ext.equals("ccl") )
  			output_ext = "xml";
  		
		int i=0;
  		try{
  			file = new File(LinerOptions.getOption(LinerOptions.OPTION_IS));
  			freader = new FileReader(file);
  			lnreader = new LineNumberReader(freader);
  			String line = "";
			while ((line = lnreader.readLine()) != null){
				i++;
				
				if ( !line.startsWith("/") )
					line = new File(file.getParent(), line).getPath();
				
				System.out.println(i+": "+line);
				StreamReader reader = ReaderFactory.get().getStreamReader(
                		line, LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
                ParagraphSet ps = reader.readParagraphSet();

                chunker.chunkInPlace(ps);
                
                File input = new File(line);                
                String output_file = new File(input.getParent(), 
                		input.getName().replaceFirst("[.][^.]+$", "") + ".ner." + output_ext).getPath();
                
        		StreamWriter writer = WriterFactory.get().getStreamWriter(output_file, output_format);
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
