package g419.spatial.pattern;

import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;

import java.util.Map;

public class SentencePatternContext {

    final SentenceAnnotationIndexTypePos annotationIndex;
    final Map<String, Annotation> matches = Maps.newHashMap();
    int currentPos;

    public SentencePatternContext(final SentenceAnnotationIndexTypePos annotationIndex, final int pos) {
        this.annotationIndex = annotationIndex;
        currentPos = pos;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    public void increaseCurrentPos() {
        currentPos++;
    }

    public void increaseCurrentPos(final int n) {
        currentPos += n;
    }

    public Sentence getSentence(){
        return annotationIndex.getSentence();
    }

    public SentenceAnnotationIndexTypePos getAnnotationIndex(){
        return annotationIndex;
    }
}
