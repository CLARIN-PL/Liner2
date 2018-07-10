package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.MappingChunker;
import org.ini4j.Ini;

/**
 * Created by michal on 9/12/14.
 */
public class ChunkerFactoryItemMapping extends ChunkerFactoryItem {

    public ChunkerFactoryItemMapping() {
        super("mapping");
    }
    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new MappingChunker(description.get("mapping"));
    }
}
