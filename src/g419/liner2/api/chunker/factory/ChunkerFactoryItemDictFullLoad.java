package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.DeserializableChunkerInterface;
import g419.liner2.api.chunker.FullDictionaryChunker;
import g419.liner2.api.tools.Logger;

import java.util.regex.Matcher;



public class ChunkerFactoryItemDictFullLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullLoad() {
		super("dict-full-load:(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Logger.log("--> Dictionary Chunker load");
            String modelFile = matcher.group(1);
            
            FullDictionaryChunker chunker = new FullDictionaryChunker();
            Logger.log("--> Loading chunker from file=" + modelFile);
			((DeserializableChunkerInterface)chunker).deserialize(modelFile);
            
            return chunker;
		}
		else		
			return null;
	}

}
