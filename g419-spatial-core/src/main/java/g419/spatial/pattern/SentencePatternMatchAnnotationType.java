package g419.spatial.pattern;

import g419.corpus.structure.Annotation;

public class SentencePatternMatchAnnotationType extends SentencePatternMatch {

    final String annotationType;

    public SentencePatternMatchAnnotationType(final String annotationType){
        this.annotationType = annotationType;
    }

    @Override
    boolean match(SentencePatternContext context, Integer begin, Integer end) {
        final Annotation an = context.getAnnotationIndex().getAnnotationOfTypeStartingFrom(annotationType, context.getCurrentPos());
        return an != null;
    }
}
