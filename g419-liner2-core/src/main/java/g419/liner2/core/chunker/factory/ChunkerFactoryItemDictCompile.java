package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.DictionaryChunker;
import org.ini4j.Ini;

import java.util.ArrayList;


public class ChunkerFactoryItemDictCompile extends ChunkerFactoryItem {

  public ChunkerFactoryItemDictCompile() {
    super("dict-compile");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    ConsolePrinter.log("--> Dictionary Chunker compile");

    final String dictFile = description.get("dict");
    final String commonsFile = description.get("common");
    final String modelFile = description.get("store");

    ArrayList<String> types = null;
    if (description.containsKey("types")) {
      types = new ArrayList<>();
      final String[] typesArray = description.get("types").split(",");
      for (int i = 0; i < typesArray.length; i++) {
        types.add(typesArray[i]);
      }
    }
    //ToDo: przenieść types do osobnego pliku? (tak jak przy crfpp)

    final DictionaryChunker chunker = new DictionaryChunker(types);
//            chunker.setModelFilename(modelFile);
    ConsolePrinter.log("--> Compiling dictionary from file=" + dictFile);
    chunker.loadDictionary(dictFile, commonsFile);
    ConsolePrinter.log("--> Saving chunker to file=" + modelFile);
    chunker.serialize(modelFile);

    return chunker;
  }

}
