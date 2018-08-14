package g419.spatial.pattern;

import com.google.common.collect.Sets;
import g419.corpus.schema.annotation.NkjpSpejd;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.SentenceAnnotationIndexTypePos;

import java.util.Set;

public class SentencePatternMatchCustomNgPrepNg extends SentencePatternMatch {

    private final Set<String> backwardSearchBreaker = Sets.newHashSet(",", ")");

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        final SentenceAnnotationIndexTypePos ai = context.getAnnotationIndex();

        final Annotation np = ai.getLongestOfTypeAtPos("chunk_np", context.getCurrentPos()).getOrNull();
        final Annotation preposition = ai.getAnnotationOfTypeStartingFrom(NkjpSpejd.Prep, context.getCurrentPos()).getOrNull();
        if (np == null || preposition == null) {
            return result;
        }

        final Annotation landmark = ai.getAnnotationOfTypeStartingFrom(NkjpSpejd.NGAny, preposition.getEnd() + 1).getOrNull();

        Integer trajectorId = preposition.getBegin() - 1;
        boolean breakSearchPrev = false;
        Annotation trajector = null;
        while (!breakSearchPrev && trajectorId >= 0 && trajector == null) {
            trajector = ai.getLongestOfTypeAtPos(NkjpSpejd.NGAny, trajectorId).getOrNull();
            breakSearchPrev = backwardSearchBreaker.contains(context.getSentence().getTokens().get(trajectorId).getOrth());
            trajectorId--;
        }

        if (np.contains(trajector) && np.contains(landmark) && np.contains(preposition)) {
            result.addAnnotation(trajector);
            result.addAnnotation(landmark);
            result.addAnnotation(preposition);
            context.setMatch(KpwrSpatial.SPATIAL_LANDMARK, landmark);
            context.setMatch(KpwrSpatial.SPATIAL_TRAJECTOR, trajector);
            context.setMatch(KpwrSpatial.SPATIAL_INDICATOR, preposition);
        }

        return result;
    }

}
