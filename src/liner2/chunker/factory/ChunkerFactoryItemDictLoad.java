package liner2.chunker.factory;

import java.util.regex.Matcher;
import java.util.ArrayList;

import liner2.chunker.Chunker;
import liner2.chunker.DictionaryChunker;
import liner2.chunker.DeserializableChunkerInterface;

import liner2.Main;

public class ChunkerFactoryItemDictLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictLoad() {
		super("dict-load:(.*?)(:types=(.*))?");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Main.log("--> Dictionary Chunker load");
            String modelFile = matcher.group(1);
            
            ArrayList<String> types = null;
            if (matcher.group(3) != null) {
            	types = new ArrayList<String>();
            	String[] typesArray = matcher.group(3).split(",");
            	for (int i = 0; i < typesArray.length; i++)
            		types.add(typesArray[i]);
            }
            
            DictionaryChunker chunker = new DictionaryChunker(types);
            Main.log("--> Loading chunker from file=" + modelFile);
			((DeserializableChunkerInterface)chunker).deserialize(modelFile);
            
            return chunker;
		}
		else		
			return null;
	}

}
