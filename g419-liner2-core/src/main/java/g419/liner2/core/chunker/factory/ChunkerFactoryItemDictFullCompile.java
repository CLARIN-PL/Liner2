package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.FullDictionaryChunker;
import org.ini4j.Ini;


public class ChunkerFactoryItemDictFullCompile extends ChunkerFactoryItem {

  public ChunkerFactoryItemDictFullCompile() {
    super("dict-full-compile");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    ConsolePrinter.log("--> Full Dictionary Chunker compile");

    final String dictFile = description.get("dict");
    final String modelFile = description.get("store");

    final FullDictionaryChunker chunker = new FullDictionaryChunker();
    ConsolePrinter.log("--> Compiling dictionary from file=" + dictFile);
    chunker.loadDictionary(dictFile);
    ConsolePrinter.log("--> Saving chunker to file=" + modelFile);
    chunker.serialize(modelFile);

    return chunker;
  }

}
