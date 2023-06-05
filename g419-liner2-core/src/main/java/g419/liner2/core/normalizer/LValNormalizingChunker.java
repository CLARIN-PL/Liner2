package g419.liner2.core.normalizer;

import g419.corpus.structure.Annotation;
import g419.liner2.core.normalizer.lval.LValRuleCompiledContainer;
import g419.liner2.core.normalizer.lval.LValRuleContainer;

import java.util.List;
import java.util.regex.Pattern;

public class LValNormalizingChunker extends NormalizingChunker {
  protected LValRuleCompiledContainer ruleContainer;

  public LValNormalizingChunker(List<Pattern> normalizedChunkTypes, LValRuleCompiledContainer ruleContainer) {
    super(normalizedChunkTypes);
    this.ruleContainer = ruleContainer;
  }

  @Override
  public void normalize(Annotation annotation) {
    if (shouldNormalize(annotation)) {
      String normalized = ruleContainer.getLVal(annotation);
      if (normalized != null) {
        annotation.setMetadata("lval", normalized);
      }
    }
  }

}
