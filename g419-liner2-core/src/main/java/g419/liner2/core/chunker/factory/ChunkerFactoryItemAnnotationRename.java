package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.lib.cli.ParameterException;
import g419.liner2.core.chunker.AnnotationRenameChunker;
import g419.liner2.core.chunker.Chunker;
import org.ini4j.Ini;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ChunkerFactoryItemAnnotationRename extends ChunkerFactoryItem {

  public ChunkerFactoryItemAnnotationRename() {
    super("annotation-rename");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    final String chunkername = description.get("base-chunker");
    final String file = description.get("file");
    final Map<String, String> rename = loadAnnotationRename(file);
    ConsolePrinter.log("--> AnnotationRenameChunker on  " + chunkername);

    final Chunker baseChunker = cm.getChunkerByName(chunkername);
    if (baseChunker == null) {
      throw new ParameterException("Undefined base chunker: " + chunkername);
    }
    return new AnnotationRenameChunker(baseChunker, rename);

  }

  /**
   * @param file
   * @return
   * @throws IOException
   */
  private Map<String, String> loadAnnotationRename(final String file) throws IOException {
    final Map<String, String> rename = new HashMap<>();
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();
    while (line != null) {
      if (!(line.isEmpty() || line.startsWith("#"))) {
        final String[] parts = line.split(":");
        if (parts.length == 2) {
          rename.put(parts[0].trim(), parts[1].trim());
        }
      }
      line = reader.readLine();
    }
    return rename;
  }
}
