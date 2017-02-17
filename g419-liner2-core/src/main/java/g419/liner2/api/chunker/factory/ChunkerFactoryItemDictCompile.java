package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.DictionaryChunker;
import g419.corpus.ConsolePrinter;
import org.ini4j.Ini;

import java.util.ArrayList;


public class ChunkerFactoryItemDictCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictCompile() {
		super("dict-compile");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        ConsolePrinter.log("--> Dictionary Chunker compile");

        String dictFile = description.get("dict");
        String commonsFile = description.get("common");
        String modelFile = description.get("store");

        ArrayList<String> types = null;
        if (description.containsKey("types")) {
            types = new ArrayList<String>();
            String[] typesArray = description.get("types").split(",");
            for (int i = 0; i < typesArray.length; i++)
                types.add(typesArray[i]);
        }
        //ToDo: przenieść types do osobnego pliku? (tak jak przy crfpp)

        DictionaryChunker chunker = new DictionaryChunker(types);
//            chunker.setModelFilename(modelFile);
        ConsolePrinter.log("--> Compiling dictionary from file=" + dictFile);
        chunker.loadDictionary(dictFile, commonsFile);
        ConsolePrinter.log("--> Saving chunker to file=" + modelFile);
        chunker.serialize(modelFile);

        return chunker;
	}

}
