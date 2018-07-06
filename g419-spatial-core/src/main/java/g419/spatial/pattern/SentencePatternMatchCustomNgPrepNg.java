package g419.spatial.pattern;

import com.google.common.collect.Sets;
import g419.corpus.schema.annotation.NkjpSpejd;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.Annotation;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;

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

//    /**
//     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
//     *
//     * @param sentence
//     */
//    public List<SpatialExpression> findCandidatesFirstNgAnyPrepNg(final Sentence sentence,
//                                                                  final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//                                                                  final Map<Integer, Annotation> chunkNpTokens,
//                                                                  final Map<Integer, Annotation> chunkPrepTokens) {
//        final List<SpatialExpression> relations = new LinkedList<>();
//        /* Szukaj wzorców NG* prep NG* */
//        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
//            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
//            if (preposition == null) {
//                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//                continue;
//            }
//
//            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
//            Integer trajectorId = an.getBegin() - 1;
//
//            boolean breakSearchPrev = false;
//            while (!breakSearchPrev && trajectorId >= 0 && mapTokenIdToAnnotations.get(trajectorId) == null) {
//                /* Przecinek i nawias zamykający przerywają poszykiwanie */
//                final String orth = sentence.getTokens().get(trajectorId).getOrth();
//                if (orth.equals(",") || orth.equals(")")) {
//                    breakSearchPrev = true;
//                }
//                trajectorId--;
//            }
//
//            if (chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
//
//                while (trajectorId > 0
//                        && mapTokenIdToAnnotations.get(trajectorId - 1) != null
//                        && chunkNpTokens.get(trajectorId - 1) == chunkNpTokens.get(landmarkId)
//                        ) {
//                    trajectorId = mapTokenIdToAnnotations.get(trajectorId - 1).get(0).getBegin();
//                }
//
//                String type = "";
//                if (trajectorId + 1 == preposition.getBegin()) {
//                    type = "<FirstNG|PrepNG>";
//                } else {
//                    type = "<FirstNG|...|PrepNG>";
//                }
//
//                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//            }
//        }
//        return relations;
//    }
}
