package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.IkarAnnotationAdderChunker;
import org.ini4j.Ini;

/*
 * @author Adam Kaczmarek
 */

public class ChunkerFactoryItemAnnotationAdder extends ChunkerFactoryItem {

  public ChunkerFactoryItemAnnotationAdder() {
    super("annotation_adder");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    return new IkarAnnotationAdderChunker(true, true, false);
  }
}
