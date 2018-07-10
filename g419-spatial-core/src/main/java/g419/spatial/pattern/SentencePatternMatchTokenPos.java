package g419.spatial.pattern;

import io.vavr.control.Option;

public class SentencePatternMatchTokenPos extends SentencePatternMatchToken {

    final String pos;

    public SentencePatternMatchTokenPos(final String pos) {
        this.pos = pos;
    }

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        Option.of(getCurrentToken(context))
                .filter(t -> t.getDisambTag().getPos().equals(pos))
                .peek(t -> addCurrentTokenToResult(context, result));
        return result;
    }

}
