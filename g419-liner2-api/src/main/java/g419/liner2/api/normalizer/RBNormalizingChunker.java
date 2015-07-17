package g419.liner2.api.normalizer;

import eu.clarin_pl.rbn.RuleSet;
import g419.corpus.structure.Annotation;

import java.util.List;
import java.util.regex.Pattern;

public class RBNormalizingChunker extends NormalizingChunker {
    protected RuleSet ruleSet;

    public RBNormalizingChunker(List<Pattern> normalizedChunkTypes, RuleSet ruleSet) {
        super(normalizedChunkTypes);
        this.ruleSet = ruleSet;
    }

    @Override
    public void normalize(Annotation annotation) {
        if (shouldNormalize(annotation)) {
            //todo: for now - only base text; in future - possibly orth form, etc
            String normalized = ruleSet.normalize(annotation.getBaseText());
//        String normalized = ruleSet.normalize(annotation.getText());
            if (normalized != null)
                //todo: future work: parametrize instance with AnnotationMetadataKey
                annotation.setMetadata("lval", normalized);
        }
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RBNormalizingChunker)) return false;
        if (!super.equals(o)) return false;

        RBNormalizingChunker that = (RBNormalizingChunker) o;

        if (!ruleSet.equals(that.ruleSet)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ruleSet.hashCode();
        return result;
    }
}
