package g419.spatial.pattern;

import g419.corpus.structure.Annotation;

public class SentencePatternMatchTokenPos extends SentencePatternMatch {

    final String pos;

    public SentencePatternMatchTokenPos(final String pos) {
        this.pos = pos;
    }

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        if (context.getSentence().getTokens().get(context.getCurrentPos()).getDisambTag().getPos().equals(pos)) {
            result.addToken(context.getCurrentPos());
            if (hasLabel()) {
                final Annotation an = new Annotation(context.getCurrentPos(), getLabel(), context.getSentence());
                context.setMatch(getLabel(), an);
            }
        }
        return result;
    }

}
