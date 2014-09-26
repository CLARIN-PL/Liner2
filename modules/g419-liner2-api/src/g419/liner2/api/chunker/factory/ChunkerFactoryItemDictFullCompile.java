package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.FullDictionaryChunker;
import g419.liner2.api.chunker.SerializableChunkerInterface;
import g419.liner2.api.tools.Logger;
import org.ini4j.Ini;

import java.util.regex.Matcher;



public class ChunkerFactoryItemDictFullCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullCompile() {
		super("dict-full-compile");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> Full Dictionary Chunker compile");

        String dictFile = description.get("dict");
        String modelFile = description.get("store");

        FullDictionaryChunker chunker = new FullDictionaryChunker();
//            chunker.setModelFilename(modelFile);
        Logger.log("--> Compiling dictionary from file=" + dictFile);
        chunker.loadDictionary(dictFile);
        Logger.log("--> Saving chunker to file=" + modelFile);
        chunker.serialize(modelFile);

        return chunker;
	}

}
