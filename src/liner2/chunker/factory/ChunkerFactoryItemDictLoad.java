package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.chunker.Chunker;
import liner2.chunker.DictionaryChunker;
import liner2.chunker.DeserializableChunkerInterface;

import liner2.Main;

public class ChunkerFactoryItemDictLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictLoad() {
		super("dict-load:(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Main.log("--> Dictionary Chunker load");
            String modelFile = matcher.group(1);
            
            DictionaryChunker chunker = new DictionaryChunker();
            Main.log("--> Loading chunker from file=" + modelFile);
			((DeserializableChunkerInterface)chunker).deserialize(modelFile);
            
            return chunker;
		}
		else		
			return null;
	}

}
