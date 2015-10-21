package g419.liner2.api.normalizer.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.normalizer.GlobalTimexNormalizer;
import org.ini4j.Ini;

public class GlobalTimexNormalizerFactoryItem extends AbstractNormalizerFactoryItem {
    public GlobalTimexNormalizerFactoryItem() {
        super("global-timex");
    }

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String update = description.get("update");
        boolean doUpdate = ( update == null || Boolean.parseBoolean(update) );
        Chunker out = new GlobalTimexNormalizer(getTypePatterns(description), doUpdate);
        return out;
    }
}
