package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.SmartDictionaryChunker;
import g419.liner2.core.tools.TrieDictNode;
import org.ini4j.Ini;


public class ChunkerFactoryItemSmartDictionary extends ChunkerFactoryItem {

	public static String PARAM_ANNOTATION = "annotation";
	public static String PARAM_DICTIONARY = "dictionary";

	public ChunkerFactoryItemSmartDictionary() {
		super("smart-dictionary");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        ConsolePrinter.log("--> Smart Dictionary Chunker load");
        String filename = description.get(PARAM_DICTIONARY);
        String annotationName = description.get(PARAM_ANNOTATION);
        TrieDictNode dictionary = TrieDictNode.loadPlain(filename);

        return new SmartDictionaryChunker(dictionary, annotationName);
	}

}
