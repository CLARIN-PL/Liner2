package g419.liner2.core.normalizer.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.normalizer.LValNormalizingChunker;
import g419.liner2.core.normalizer.lval.LValRuleContainer;
import org.ini4j.Ini;

//current LVAL normalizer
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
