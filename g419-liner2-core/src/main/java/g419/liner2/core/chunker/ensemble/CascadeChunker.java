package g419.liner2.core.chunker.ensemble;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.normalizer.Normalizer;
import g419.liner2.core.normalizer.NormalizingChunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author Michał Marcińczuk
 */
public class CascadeChunker extends Chunker implements Normalizer {

    private final ArrayList<Chunker> chunkers;

    public CascadeChunker(final ArrayList<Chunker> chunkers) {
        this.chunkers = chunkers;
    }

    @Override
    public void onNewDocument(final Document document) {
        for (final Chunker c : chunkers) {
            if (c instanceof NormalizingChunker) {
                ((NormalizingChunker) c).onNewDocument(document);
            }
        }

    }

    @Override
    public void onDocumentEnd(final Document document) {
        for (final Chunker c : chunkers) {
            if (c instanceof NormalizingChunker) {
                ((NormalizingChunker) c).onDocumentEnd(document);
            }
        }
    }

    @Override
    public void onNewSentence(final Sentence sentence) {
        for (final Chunker c : chunkers) {
            if (c instanceof NormalizingChunker) {
                ((NormalizingChunker) c).onNewSentence(sentence);
            }
        }
    }

    @Override
    public void onSentenceEnd(final Sentence sentence) {
        for (final Chunker c : chunkers) {
            if (c instanceof NormalizingChunker) {
                ((NormalizingChunker) c).onSentenceEnd(sentence);
            }
        }
    }

    @Override
    public void onNewAnnotation(final Annotation annotation) {
        for (final Chunker c : chunkers) {
            if (c instanceof NormalizingChunker) {
                ((NormalizingChunker) c).onNewAnnotation(annotation);
            }
        }
    }

    @Override
    public void onAnnotationEnd(final Annotation annotation) {
        for (final Chunker c : chunkers) {
            if (c instanceof NormalizingChunker) {
                ((NormalizingChunker) c).onAnnotationEnd(annotation);
            }
        }
    }

    @Override
    public HashMap<Sentence, AnnotationSet> chunk(final Document ps) {
        final HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>();

        final Document document = ps.clone();

        for (final Chunker chunker : this.chunkers) {
            chunker.chunkInPlace(document);
        }

        for (int i = 0; i < document.getSentences().size(); i++) {
            final Sentence sentence = ps.getSentences().get(i);
            final AnnotationSet set = new AnnotationSet(sentence);
            for (final Annotation an : document.getSentences().get(i).getChunks()) {
                if (!sentence.getChunks().contains(an)) {
                    an.setSentence(sentence);
                    set.addChunk(an);
                } else {
                    // TODO
                    // System.out.println("Ignore: " + an.toString());
                }
            }
            chunkings.put(sentence, set);
        }

        // Dodaj relacje - dla np. ChunkRel'a
        ps.setRelations(document.getRelations());

        return chunkings;
    }

    @Override
    public List<Pattern> getNormalizedChunkTypes() {
        final List<Pattern> out = new ArrayList<>();
        for (final Chunker c : chunkers) {
            if (c instanceof Normalizer) {
                out.addAll(((Normalizer) c).getNormalizedChunkTypes());
            }
        }
        return out;
    }

    @Override
    public void normalize(final Annotation annotation) {
        for (final Chunker c : chunkers) {
            if (c instanceof Normalizer) {
                ((Normalizer) c).normalize(annotation);
            }
        }
    }
}
