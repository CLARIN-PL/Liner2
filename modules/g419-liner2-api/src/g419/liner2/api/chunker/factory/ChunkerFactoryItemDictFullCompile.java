package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.FullDictionaryChunker;
import g419.liner2.api.chunker.SerializableChunkerInterface;
import g419.liner2.api.tools.Logger;

import java.util.regex.Matcher;



public class ChunkerFactoryItemDictFullCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullCompile() {
		super("dict-full-compile:dict=(.*):model=(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Logger.log("--> Full Dictionary Chunker compile");

            String dictFile = matcher.group(1);
            String modelFile = matcher.group(2);
            
            FullDictionaryChunker chunker = new FullDictionaryChunker();
//            chunker.setModelFilename(modelFile);
            Logger.log("--> Compiling dictionary from file=" + dictFile);
            chunker.loadDictionary(dictFile);
            Logger.log("--> Saving chunker to file=" + modelFile);
			((SerializableChunkerInterface)chunker).serialize(modelFile);
            
            return chunker;		
		}
		else		
			return null;
	}

}
