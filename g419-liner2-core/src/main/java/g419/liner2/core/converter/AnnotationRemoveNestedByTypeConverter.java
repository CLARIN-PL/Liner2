package g419.liner2.core.converter;


import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.SentenceAnnotationIndexTypePos;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationRemoveNestedByTypeConverter extends Converter {

    private final Set<String> typesUnconditioned;
    private final Map<String, Set<String>> typesConditionedByOuterType;

    public AnnotationRemoveNestedByTypeConverter(final Set<String> typesUnconditioned, final Map<String, Set<String>> typesConditionedByOuterType) {
        this.typesUnconditioned = typesUnconditioned;
        this.typesConditionedByOuterType = typesConditionedByOuterType;
    }

    @Override
    public void apply(final Sentence sentence) {
        final SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence);
        final Set<Annotation> toRemove = sentence.getChunks().stream()
                .filter(an -> isToRemove(an, index))
                .collect(Collectors.toSet());
        sentence.getChunks().removeAll(toRemove);
    }

    private boolean isToRemove(final Annotation an, final SentenceAnnotationIndexTypePos index) {
        final Set<String> outerTypes = index.getAtPos(an.getBegin()).stream()
                .filter(a -> a != an)
                .filter(a -> a.contains(an))
                .map(Annotation::getType)
                .collect(Collectors.toSet());
        if (typesUnconditioned.contains(an.getType()) && outerTypes.size() > 0) {
            return true;
        }

        outerTypes.retainAll(typesConditionedByOuterType.computeIfAbsent(an.getType(), k -> Sets.newHashSet()));
        return outerTypes.size() > 0;
    }

}
