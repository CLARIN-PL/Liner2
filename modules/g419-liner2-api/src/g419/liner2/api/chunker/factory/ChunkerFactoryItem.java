package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;

import java.util.regex.Pattern;



public abstract class ChunkerFactoryItem {

	protected Pattern pattern = null;
	
	public ChunkerFactoryItem(String stringPattern){
		pattern = Pattern.compile("^"+stringPattern+"$");
	}
	
	public Pattern getPattern(){
		return this.pattern;
	}
	
	abstract public Chunker getChunker(String description, ChunkerManager cm) throws Exception ;

}
