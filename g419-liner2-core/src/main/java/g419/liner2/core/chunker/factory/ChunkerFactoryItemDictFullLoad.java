package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.FullDictionaryChunker;
import org.ini4j.Ini;


public class ChunkerFactoryItemDictFullLoad extends ChunkerFactoryItem {

  public ChunkerFactoryItemDictFullLoad() {
    super("dict-full-load");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    ConsolePrinter.log("--> Dictionary Chunker load");
    final String modelFile = description.get("store");

    final FullDictionaryChunker chunker = new FullDictionaryChunker();
    ConsolePrinter.log("--> Loading chunker from file=" + modelFile);
    chunker.deserialize(modelFile);

    return chunker;
  }

}
