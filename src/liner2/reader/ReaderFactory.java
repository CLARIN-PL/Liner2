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
	public StreamReader getStreamReader(String inputFile, String inputFormat){
		//String inputFormat = options.getOption(options.OPTION_INPUT_FORMAT);
		//String inputFile = options.getOption(options.OPTION_INPUT_FILE);
		if (inputFormat.equals("ccl"))
			return new CclStreamReader(inputFile);
		else if (inputFormat.equals("iob"))
			return new IobStreamReader(inputFile);
		else		
			return null;
	}
	
}
