package liner2.writer;

import liner2.writer.CclStreamWriter;
import liner2.writer.IobStreamWriter;

import liner2.LinerOptions;

public class WriterFactory {

	private static final WriterFactory factory = new WriterFactory();
	
	private WriterFactory(){
		
	}
	
	public static WriterFactory get(){
		return WriterFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public StreamWriter getStreamWriter(String outputFile, String outputFormat){
//		String outputFormat = options.getOption(options.OPTION_OUTPUT_FORMAT);
//		String outputFile = options.getOption(options.OPTION_OUTPUT_FILE);
		if (outputFormat.equals("ccl"))
			return new CclStreamWriter(outputFile);
		else if (outputFormat.equals("iob"))
			return new IobStreamWriter(outputFile);
		else		
			return null;
	}
	
}
