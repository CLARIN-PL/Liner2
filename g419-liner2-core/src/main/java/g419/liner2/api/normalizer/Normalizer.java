package g419.liner2.api.normalizer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.List;
import java.util.regex.Pattern;

public interface Normalizer {
    public List<Pattern> getNormalizedChunkTypes();
    /**
     * Normalize given annotation in place. This will probably mean adding some metadata to it.
     * @param annotation Annotation (chunk) to be normalized.
     */
    public void normalize(Annotation annotation);

    //todo: those should be in "ObservableNormalizer" or smth like that - refactor in future
    public void onNewDocument(Document document);
    public void onDocumentEnd(Document document);

    public void onNewSentence(Sentence sentence);
    public void onSentenceEnd(Sentence sentence);

    public void onNewAnnotation(Annotation annotation);
    public void onAnnotationEnd(Annotation annotation);
}
