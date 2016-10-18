package g419.liner2.api.normalizer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.chunker.Chunker;

import javax.print.Doc;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizer is special kind of chunker that will not add new annotations, but enrich them with metadata instead.
 */
abstract public class NormalizingChunker extends Chunker implements Normalizer {
    /**
     * Normalizer should only enhance annotations which have type (chunk.getType())
     * matching one of those patterns.
     */
    protected List<Pattern> normalizedChunkTypes;

    public NormalizingChunker(List<Pattern> normalizedChunkTypes) {
        this.normalizedChunkTypes = normalizedChunkTypes;
    }

    public List<Pattern> getNormalizedChunkTypes() {
        return normalizedChunkTypes;
    }

    public void setNormalizedChunkTypes(List<Pattern> normalizedChunkTypes) {
        this.normalizedChunkTypes = normalizedChunkTypes;
    }


    abstract public void normalize(Annotation annotation);

    protected boolean shouldNormalize(Annotation annotation){
        for (Pattern p: normalizedChunkTypes)
            if (p.matcher(annotation.getType()).find())
                return true;
        return false;
    }

    public void onNewDocument(Document document){}
    public void onDocumentEnd(Document document){}

    public void onNewSentence(Sentence sentence){}
    public void onSentenceEnd(Sentence sentence){}

    public void onNewAnnotation(Annotation annotation){}
    public void onAnnotationEnd(Annotation annotation){}

    @Override
    public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
        onNewDocument(ps);
        HashMap<Sentence, AnnotationSet> out = new HashMap<>();
        for (Sentence sentence : ps.getSentences()) {
            onNewSentence(sentence);
            out.put(sentence, new AnnotationSet(sentence));
            annotationLoop:
            for (Annotation annotation : sentence.getChunks()) {
                onNewAnnotation(annotation);
                String type = annotation.getType();
                for (Pattern typePattern : normalizedChunkTypes) {
                    Matcher matcher = typePattern.matcher(type);
                    matcher.find();
                    if (matcher.matches()) {
                        normalize(annotation);
                        onAnnotationEnd(annotation);
                        continue annotationLoop;
                    }
                }
                onAnnotationEnd(annotation);
            }
            onSentenceEnd(sentence);
        }
        onDocumentEnd(ps);
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NormalizingChunker)) return false;

        NormalizingChunker that = (NormalizingChunker) o;

        if (!normalizedChunkTypes.equals(that.normalizedChunkTypes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return normalizedChunkTypes.hashCode();
    }
}
