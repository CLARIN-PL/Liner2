package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.chunker.Chunker;
import liner2.chunker.FullDictionaryChunker;
import liner2.chunker.DeserializableChunkerInterface;

import liner2.Main;

public class ChunkerFactoryItemDictFullLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullLoad() {
		super("dict-full-load:(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Main.log("--> Dictionary Chunker load");
            String modelFile = matcher.group(1);
            
            FullDictionaryChunker chunker = new FullDictionaryChunker();
            Main.log("--> Loading chunker from file=" + modelFile);
			((DeserializableChunkerInterface)chunker).deserialize(modelFile);
            
            return chunker;
		}
		else		
			return null;
	}

}
