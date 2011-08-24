package liner2.reader;

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
	 * @param options
	 * @return
	 */
	public StreamReader getStreamReader(LinerOptions options){
		return null;
	}
	
}
