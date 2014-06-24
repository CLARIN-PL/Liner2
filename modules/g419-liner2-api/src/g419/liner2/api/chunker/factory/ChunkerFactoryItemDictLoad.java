package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.DeserializableChunkerInterface;
import g419.liner2.api.chunker.DictionaryChunker;
import g419.liner2.api.tools.Logger;

import java.util.ArrayList;
import java.util.regex.Matcher;



public class ChunkerFactoryItemDictLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictLoad() {
		super("dict-load:(.*?)(:types=(.*))?");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Logger.log("--> Dictionary Chunker load");
            String modelFile = matcher.group(1);
            
            ArrayList<String> types = null;
            if (matcher.group(3) != null) {
            	types = new ArrayList<String>();
            	String[] typesArray = matcher.group(3).split(",");
            	for (int i = 0; i < typesArray.length; i++)
            		types.add(typesArray[i]);
            }
            
            DictionaryChunker chunker = new DictionaryChunker(types);
            Logger.log("--> Loading chunker from file=" + modelFile);
			((DeserializableChunkerInterface)chunker).deserialize(modelFile);
            
            return chunker;
		}
		else		
			return null;
	}

}
