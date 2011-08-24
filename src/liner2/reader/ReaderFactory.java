package liner2.reader;

import liner2.reader.CclStreamReader;
import liner2.reader.IobStreamReader;

import liner2.LinerOptions;

public class ReaderFactory {

	private static final ReaderFactory factory = new ReaderFactory();
	
	private ReaderFactory(){
		
	}
	
	public static ReaderFactory get(){
		return ReaderFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public StreamReader getStreamReader(LinerOptions options){
		if (options.inputFormat.equals("ccl"))
			return new CclStreamReader(options.inputFile);
		else if (options.inputFormat.equals("iob"))
			return new IobStreamReader(options.inputFile);
		else		
			return null;
	}
	
}
