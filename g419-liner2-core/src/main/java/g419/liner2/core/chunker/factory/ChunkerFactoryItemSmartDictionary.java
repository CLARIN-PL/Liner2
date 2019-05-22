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
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    ConsolePrinter.log("--> Smart Dictionary Chunker load");
    final String filename = description.get(PARAM_DICTIONARY);
    final String annotationName = description.get(PARAM_ANNOTATION);
    final TrieDictNode dictionary = TrieDictNode.loadPlain(filename);

    return new SmartDictionaryChunker(dictionary, annotationName);
  }

}
