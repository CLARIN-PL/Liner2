package g419.liner2.api.normalizer;

import g419.corpus.structure.Annotation;
import g419.liner2.api.normalizer.rbn.RuleSet;

import java.util.List;
import java.util.regex.Pattern;

import g419.liner2.api.normalizer.lval.LValRuleContainer;

public class LValNormalizingChunker extends NormalizingChunker {
    protected LValRuleContainer ruleContainer;

    public LValNormalizingChunker(List<Pattern> normalizedChunkTypes, LValRuleContainer ruleContainer) {
        super(normalizedChunkTypes);
        this.ruleContainer = ruleContainer;
    }

    @Override
    public void normalize(Annotation annotation) {
        if (shouldNormalize(annotation)) {
            String baseText = annotation.getSimpleBaseText();
            String type = annotation.getType();
            String normalized = ruleContainer.getLVal(annotation.getSimpleBaseText(), annotation.getType());
            if (normalized != null)
                annotation.setMetadata("lval", normalized);
        }
    }

}
