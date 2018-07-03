package g419.spatial.pattern;

import io.vavr.control.Option;

import java.util.regex.Pattern;

public class SentencePatternMatchAnnotationPattern extends SentencePatternMatch {

    final Pattern annotationTypePattern;

    public SentencePatternMatchAnnotationPattern(final Pattern annotationTypePattern) {
        this.annotationTypePattern = annotationTypePattern;
    }

    @Override
    SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end) {
        final SentencePatternResult result = new SentencePatternResult();
        Option.of(context.getAnnotationIndex().getAnnotationOfTypeStartingFrom(annotationTypePattern, context.getCurrentPos()))
                .peek(an -> {
                    result.addAnnotation(an);
                    if (hasLabel()) {
                        context.setMatch(getLabel(), an);
                    }
                });
        return result;
    }
}
