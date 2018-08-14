package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.SmartDictionaryChunker;
import org.ini4j.Ini;


public class ChunkerFactoryItemSmartDictionaryCompile extends ChunkerFactoryItem {

	public static String PARAM_ANNOTATION = "annotation";
	public static String PARAM_DICTIONARY = "dictionary";

	public ChunkerFactoryItemSmartDictionaryCompile() {
		super("smart-dictionary-compile");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        ConsolePrinter.log("--> Smart Dictionary Chunker load");
        String filename = description.get(PARAM_DICTIONARY);        
        
        return SmartDictionaryChunker.compile(filename);
	}

}
