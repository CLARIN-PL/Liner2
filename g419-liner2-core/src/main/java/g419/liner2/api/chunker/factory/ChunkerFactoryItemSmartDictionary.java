package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.DictionaryChunker;
import g419.liner2.api.chunker.SmartDictionaryChunker;
import g419.liner2.api.tools.TrieDictNode;
import g419.corpus.Logger;
import org.ini4j.Ini;

import java.util.ArrayList;


public class ChunkerFactoryItemSmartDictionary extends ChunkerFactoryItem {

	public static String PARAM_ANNOTATION = "annotation";
	public static String PARAM_DICTIONARY = "dictionary";

	public ChunkerFactoryItemSmartDictionary() {
		super("smart-dictionary");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> Smart Dictionary Chunker load");
        String filename = description.get(PARAM_DICTIONARY);
        String annotationName = description.get(PARAM_ANNOTATION);
        TrieDictNode dictionary = TrieDictNode.loadPlain(filename);

        return new SmartDictionaryChunker(dictionary, annotationName);
	}

}
