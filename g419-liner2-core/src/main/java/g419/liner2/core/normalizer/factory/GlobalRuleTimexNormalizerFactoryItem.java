package g419.liner2.core.normalizer.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.normalizer.RuleGlobalTimexNormalizer;
import org.ini4j.Ini;

//current VAL normalizer
public class GlobalRuleTimexNormalizerFactoryItem extends AbstractNormalizerFactoryItem {
    public GlobalRuleTimexNormalizerFactoryItem() {
        super("global-rule-timex");
    }

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Chunker out = new RuleGlobalTimexNormalizer(getTypePatterns(description));
        return out;
    }
}
