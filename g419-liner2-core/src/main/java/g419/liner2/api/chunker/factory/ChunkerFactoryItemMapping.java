package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.MappingChunker;
import org.ini4j.Ini;

import java.util.regex.Matcher;

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
