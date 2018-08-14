package g419.spatial.pattern;

public class SentencePatternMatchAnnotationType extends SentencePatternMatch {

    final String annotationType;

    public SentencePatternMatchAnnotationType(final String annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        context.getAnnotationIndex().getAnnotationOfTypeStartingFrom(annotationType, context.getCurrentPos())
                .peek(an -> {
                    result.addAnnotation(an);
                    if (hasLabel()) {
                        context.setMatch(getLabel(), an);
                    }
                });
        return result;
    }
}
