package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.FullDictionaryChunker;
import g419.corpus.Logger;
import org.ini4j.Ini;


public class ChunkerFactoryItemDictFullLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictFullLoad() {
		super("dict-full-load");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> Dictionary Chunker load");
        String modelFile = description.get("store");

        FullDictionaryChunker chunker = new FullDictionaryChunker();
        Logger.log("--> Loading chunker from file=" + modelFile);
        chunker.deserialize(modelFile);

        return chunker;
	}

}
