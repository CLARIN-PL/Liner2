package g419.liner2.core.chunker.ensemble;

import g419.corpus.structure.*;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.features.TokenFeatureGenerator;
import g419.liner2.core.normalizer.Normalizer;
import g419.liner2.core.normalizer.NormalizingChunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author Jan Kocoń
 */
public class OverwriteChunker extends Chunker implements Normalizer {

    private ArrayList<Chunker> chunkers;

    public OverwriteChunker(ArrayList<Chunker> chunkers) {
        this.chunkers = chunkers;
    }

    public void onNewDocument(Document document) {
        for (Chunker c : chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onNewDocument(document);

    }

    public void onDocumentEnd(Document document) {
        for (Chunker c : chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onDocumentEnd(document);
    }

    public void onNewSentence(Sentence sentence) {
        for (Chunker c : chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onNewSentence(sentence);
    }

    public void onSentenceEnd(Sentence sentence) {
        for (Chunker c : chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onSentenceEnd(sentence);
    }

    public void onNewAnnotation(Annotation annotation) {
        for (Chunker c : chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onNewAnnotation(annotation);
    }

    public void onAnnotationEnd(Annotation annotation) {
        for (Chunker c : chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onAnnotationEnd(annotation);
    }

    public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
        HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
        Document document = ps.clone();

        for (Chunker chunker : this.chunkers) {
            try {
                this.getFeatureGenerator().generateFeatures(document);
            } catch (Exception e) {
                e.printStackTrace();
            }
            chunker.chunkInPlace(document);


        }
        for (int i = 0; i < document.getSentences().size(); i++) {
            Sentence sentence = ps.getSentences().get(i);
            AnnotationSet set = new AnnotationSet(sentence);
            for (Annotation an : document.getSentences().get(i).getChunks()) {
                if (!sentence.getChunks().contains(an)) {
                    an.setSentence(sentence);
                    set.addChunk(an);
                } else {
                    //replace old annotations with new - may change in metadata
                    sentence.getChunks().remove(an);
                    an.setSentence(sentence);
                    set.addChunk(an);
                }
            }
            if (chunkings.containsKey(sentence))
                chunkings.get(sentence).union(set);
            else
                chunkings.put(sentence, set);
        }

        // Dodaj relacje - dla np. ChunkRel'a
        ps.setRelations(document.getRelations());

        return chunkings;
    }

    @Override
    public List<Pattern> getNormalizedChunkTypes() {
        List<Pattern> out = new ArrayList<>();
        for (Chunker c : chunkers)
            if (c instanceof Normalizer)
                out.addAll(((Normalizer) c).getNormalizedChunkTypes());
        return out;
    }

    @Override
    public void normalize(Annotation annotation) {
        for (Chunker c : chunkers)
            if (c instanceof Normalizer)
                ((Normalizer) c).normalize(annotation);
    }
}
