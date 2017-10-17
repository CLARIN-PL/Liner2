package g419.liner2.core.chunker.factory;


import g419.liner2.core.chunker.Chunker;
import org.ini4j.Ini;


public abstract class ChunkerFactoryItem {

	protected String type = null;
	
	public ChunkerFactoryItem(String chunkerType){
		type = chunkerType;
	}
	
	public String getType(){
		return this.type;
	}
	
	abstract public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception ;

}
