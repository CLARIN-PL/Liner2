package g419.liner2.api.normalizer.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.normalizer.LValNormalizingChunker;
import g419.liner2.api.normalizer.lval.LValRuleContainer;
import org.ini4j.Ini;

public class LValNormalizerFactoryItem extends AbstractNormalizerFactoryItem {
    public LValNormalizerFactoryItem() {
        super("lval-norm");
    }

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String path = description.get("ruleSet");
        Chunker out = new LValNormalizingChunker(getTypePatterns(description), LValRuleContainer.load(path));
        return out;
    }
}
