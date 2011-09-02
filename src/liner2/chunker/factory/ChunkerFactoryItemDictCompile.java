package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.chunker.Chunker;
import liner2.chunker.DictionaryChunker;
import liner2.chunker.SerializableChunkerInterface;

import liner2.Main;

public class ChunkerFactoryItemDictCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictCompile() {
		super("dict-compile:dict=(.*):common=(.*):model=(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Main.log("--> Dictionary Chunker compile");

            String dictFile = matcher.group(1);
            String commonsFile = matcher.group(2);
            String modelFile = matcher.group(3);
            
            DictionaryChunker chunker = new DictionaryChunker();
//            chunker.setModelFilename(modelFile);
            Main.log("--> Compiling dictionary from file=" + dictFile);
            chunker.loadDictionary(dictFile, commonsFile);
            Main.log("--> Saving chunker to file=" + modelFile);
			((SerializableChunkerInterface)chunker).serialize(modelFile);
            
            return chunker;		
		}
		else		
			return null;
	}

}
