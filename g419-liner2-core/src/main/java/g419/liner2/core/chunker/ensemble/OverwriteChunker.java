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
import java.util.function.BiConsumer;

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
      chunkings.computeIfPresent(sentence, (k, v) -> {
        v.union(set);
        return v;
      });
      chunkings.putIfAbsent(sentence, set);
    }

    // Add relation - e.g. for ChunkRel
    ps.setRelations(document.getRelations());

    return chunkings;
  }

  private <T> void triggerNormalizingChunkerMethod(final BiConsumer<NormalizingChunker, T> method, final T element) {
    chunkers.stream()
            .filter(c -> c instanceof NormalizingChunker)
            .map(c -> (NormalizingChunker) c)
            .peek(c -> method.accept(c, element));
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

  @Override
  public void onNewDocument(final Document document) {
    triggerNormalizingChunkerMethod(NormalizingChunker::onNewDocument, document);
  }

  @Override
  public void onDocumentEnd(final Document document) {
    triggerNormalizingChunkerMethod(NormalizingChunker::onDocumentEnd, document);
  }

  @Override
  public void onNewSentence(final Sentence sentence) {
    triggerNormalizingChunkerMethod(NormalizingChunker::onNewSentence, sentence);
  }

  @Override
  public void onSentenceEnd(final Sentence sentence) {
    triggerNormalizingChunkerMethod(NormalizingChunker::onSentenceEnd, sentence);
  }

  @Override
  public void onNewAnnotation(final Annotation annotation) {
    triggerNormalizingChunkerMethod(NormalizingChunker::onNewAnnotation, annotation);
  }

  @Override
  public void onAnnotationEnd(final Annotation annotation) {
    triggerNormalizingChunkerMethod(NormalizingChunker::onAnnotationEnd, annotation);
  }
}
