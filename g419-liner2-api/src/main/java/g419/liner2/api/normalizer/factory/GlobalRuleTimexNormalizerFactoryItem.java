package g419.liner2.api.normalizer.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.normalizer.GlobalTimexNormalizer;
import g419.liner2.api.normalizer.RuleGlobalTimexNormalizer;
import org.ini4j.Ini;

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
