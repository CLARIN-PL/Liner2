package g419.spatial.pattern;

import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Frame;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.SentenceAnnotationIndexTypePos;

import java.util.Map;

public class SentencePatternContext {

    final SentenceAnnotationIndexTypePos annotationIndex;
    final Map<String, Annotation> matches = Maps.newHashMap();
    int currentPos;

    public SentencePatternContext(final SentenceAnnotationIndexTypePos annotationIndex, final int pos) {
        this.annotationIndex = annotationIndex;
        currentPos = pos;
    }

    public void setMatch(final String label, final Annotation annotation) {
        matches.put(label, annotation);
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(final int currentPos) {
        this.currentPos = currentPos;
    }

    public void increaseCurrentPos() {
        currentPos++;
    }

    public void increaseCurrentPos(final int n) {
        currentPos += n;
    }

    public Sentence getSentence() {
        return annotationIndex.getSentence();
    }

    public SentenceAnnotationIndexTypePos getAnnotationIndex() {
        return annotationIndex;
    }

    public Frame<Annotation> toFrame(final String type) {
        final Frame<Annotation> frame = new Frame<>(type);
        matches.entrySet().stream().forEach(p -> frame.set(p.getKey(), p.getValue()));
        return frame;
    }
}
