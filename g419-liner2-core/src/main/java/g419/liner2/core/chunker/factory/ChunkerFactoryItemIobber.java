package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.IobberChunker;
import org.ini4j.Profile.Section;

public class ChunkerFactoryItemIobber extends ChunkerFactoryItem {

  public ChunkerFactoryItemIobber() {
    super("iobber");
  }

  @Override
  public Chunker getChunker(Section description, ChunkerManager cm) throws Exception {
    return new IobberChunker(description.get("iobber_path"), description.get("iobber_model"), description.get("iobber_ini_path"));
  }

}
