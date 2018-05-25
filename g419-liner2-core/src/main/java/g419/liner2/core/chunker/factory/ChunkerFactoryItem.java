package g419.liner2.core.chunker.factory;


import g419.liner2.core.chunker.Chunker;
import org.ini4j.Ini;


public abstract class ChunkerFactoryItem {

	final protected String type;
	
	public ChunkerFactoryItem(final String chunkerType){
		type = chunkerType;
	}
	
	public String getType(){
		return this.type;
	}
	
	abstract public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception ;

	protected String getParameterString(final Ini.Section description, final String name) throws ChunkerFactoryItemParameterNotFoundException{
		if (description.containsKey(name)){
			return description.get(name);
		} else {
			throw new ChunkerFactoryItemParameterNotFoundException(description, name);
		}
	}

}
