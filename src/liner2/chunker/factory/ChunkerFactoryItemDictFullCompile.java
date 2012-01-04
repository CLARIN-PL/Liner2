package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.chunker.Chunker;
import liner2.chunker.FullDictionaryChunker;
import liner2.chunker.SerializableChunkerInterface;

import liner2.Main;

public class ChunkerFactoryItemDictFullCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullCompile() {
		super("dict-full-compile:dict=(.*):model=(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Main.log("--> Full Dictionary Chunker compile");

            String dictFile = matcher.group(1);
            String modelFile = matcher.group(2);
            
            FullDictionaryChunker chunker = new FullDictionaryChunker();
//            chunker.setModelFilename(modelFile);
            Main.log("--> Compiling dictionary from file=" + dictFile);
            chunker.loadDictionary(dictFile);
            Main.log("--> Saving chunker to file=" + modelFile);
			((SerializableChunkerInterface)chunker).serialize(modelFile);
            
            return chunker;		
		}
		else		
			return null;
	}

}
