package g419.spatial.pattern;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;

public abstract class SentencePatternMatchToken extends SentencePatternMatch {

    protected Token getCurrentToken(final SentencePatternContext context) {
        return context.getCurrentPos() < context.getSentence().getTokens().size()
                ? context.getSentence().getTokens().get(context.getCurrentPos())
                : null;
    }

    protected void addCurrentTokenToResult(final SentencePatternContext context, final SentencePatternResult result) {
        result.addToken(context.getCurrentPos());
        if (hasLabel()) {
            context.setMatch(getLabel(), new Annotation(context.getCurrentPos(), getLabel(), context.getSentence()));
        }
    }
}
