package g419.spatial.pattern;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class SentencePatternMatchSequence extends SentencePatternMatch {

    final List<SentencePatternMatch> matchers = Lists.newArrayList();

    public SentencePatternMatchSequence append(final SentencePatternMatch match) {
        matchers.add(match);
        return this;
    }

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        final Iterator<SentencePatternMatch> itMatchers = matchers.iterator();
        boolean matches = true;
        while (itMatchers.hasNext() && matches) {
            final SentencePatternResult resultPart = itMatchers.next().match(context, null, null);
            result.mergeWith(resultPart);
            matches = resultPart.matched();
            if (matches) {
                context.setCurrentPos(resultPart.getMatchEnd() + 1);
            }
        }
        return matches ? result : new SentencePatternResult();
    }
}
