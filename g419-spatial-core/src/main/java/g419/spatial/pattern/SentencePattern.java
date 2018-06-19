package g419.spatial.pattern;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Frame;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SentencePattern {

    final private String type;
    final private List<SentencePatternMatch> matchers;

    public SentencePattern(final String type, final List<SentencePatternMatch> matchers){
        this.type = type;
        this.matchers = matchers;
    }

    public List<Frame<Annotation>> match(final SentenceAnnotationIndexTypePos annotations) {
        return IntStream.range(0, annotations.getSentence().getTokens().size())
                .mapToObj(i -> match(annotations, i))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Frame<Annotation> match(final SentenceAnnotationIndexTypePos annotations, final int pos) {
        final SentencePatternContext context = new SentencePatternContext(annotations, pos);
        final Iterator<SentencePatternMatch> itMatchers = matchers.iterator();
        boolean matches = true;
        while ( itMatchers.hasNext() && matches ){
            matches = itMatchers.next().match(context, null, null);
        }
        return matches ? createFrame(context) : null;
    }

    private Frame<Annotation> createFrame(final SentencePatternContext context){
        return new Frame<>(type);
    }
}
