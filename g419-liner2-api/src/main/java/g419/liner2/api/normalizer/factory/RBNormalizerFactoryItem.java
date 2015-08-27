package g419.liner2.api.normalizer.factory;

import g419.liner2.api.normalizer.rbn.RuleSetLoader;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.normalizer.RBNormalizingChunker;
import org.ini4j.Ini;

public class RBNormalizerFactoryItem extends AbstractNormalizerFactoryItem {
    public RBNormalizerFactoryItem() {
        super("rbn");
    }

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String path = description.get("ruleSet");
        Chunker out = new RBNormalizingChunker(getTypePatterns(description), RuleSetLoader.getInstance().load(path));
        return out;
    }
}
