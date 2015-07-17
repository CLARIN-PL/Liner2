package g419.liner2.api.normalizer.factory;

import eu.clarin_pl.rbn.RuleSetLoader;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactoryItem;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.normalizer.GlobalTimexNormalizer;
import g419.liner2.api.normalizer.RBNormalizingChunker;
import org.ini4j.Ini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
