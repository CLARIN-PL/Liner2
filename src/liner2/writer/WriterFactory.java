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
	public StreamWriter getStreamWriter(LinerOptions options){
		if (options.outputFormat.equals("ccl"))
			return new CclStreamWriter(options.outputFile);
		else if (options.outputFormat.equals("iob"))
			return new IobStreamWriter(options.outputFile);
		else		
			return null;
	}
	
}
