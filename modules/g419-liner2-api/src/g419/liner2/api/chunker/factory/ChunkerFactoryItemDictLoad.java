package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.DeserializableChunkerInterface;
import g419.liner2.api.chunker.DictionaryChunker;
import g419.liner2.api.tools.Logger;
import org.ini4j.Ini;

import java.util.ArrayList;
import java.util.regex.Matcher;



public class ChunkerFactoryItemDictLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictLoad() {
		super("dict-load");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> Dictionary Chunker load");
        String modelFile = description.get("store");

        ArrayList<String> types = null;
        if (description.containsKey("types")) {
            types = new ArrayList<String>();
            String[] typesArray = description.get("types").split(",");
            for (int i = 0; i < typesArray.length; i++)
                types.add(typesArray[i]);
        }

        DictionaryChunker chunker = new DictionaryChunker(types);
        Logger.log("--> Loading chunker from file=" + modelFile);
        chunker.deserialize(modelFile);

        return chunker;
	}

}
