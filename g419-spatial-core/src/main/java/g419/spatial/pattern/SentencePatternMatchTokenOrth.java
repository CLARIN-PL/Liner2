package g419.spatial.pattern;

import io.vavr.control.Option;

public class SentencePatternMatchTokenOrth extends SentencePatternMatchToken {

    final String orth;

    public SentencePatternMatchTokenOrth(final String orth) {
        this.orth = orth;
    }

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        Option.of(getCurrentToken(context))
                .filter(t -> t.getOrth().equals(orth))
                .peek(t -> addCurrentTokenToResult(context, result));
        return result;
    }

}
