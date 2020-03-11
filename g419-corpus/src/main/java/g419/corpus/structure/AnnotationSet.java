package g419.corpus.structure;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

public class AnnotationSet {

  LinkedHashSet<Annotation> chunks = new LinkedHashSet<>();
  Sentence sentence = null;

  public AnnotationSet(final Sentence sentence, final LinkedHashSet<Annotation> chunks) {
    this.chunks = chunks;
    this.sentence = sentence;
  }

  public AnnotationSet(final Sentence sentence) {
    this.sentence = sentence;
  }

  public void addChunk(final Annotation chunk) {
    chunks.add(chunk);
  }

  public void removeChunk(final Annotation chunk) {
    chunks.remove(chunk);
  }

  public LinkedHashSet<Annotation> chunkSet() {
    return chunks;
  }

  public Sentence getSentence() {
    return sentence;
  }

  public boolean contains(final Annotation chunk) {
    return chunks.contains(chunk);
  }

  public void union(final AnnotationSet foreignChunking) {

    if (foreignChunking.getSentence() == sentence) {
      final HashSet<Annotation> foreignChunks = foreignChunking.chunkSet();
      final Iterator<Annotation> i_fc = foreignChunks.iterator();
      while (i_fc.hasNext()) {
        final Annotation foreignChunk = i_fc.next();
        boolean found = false;
        final HashSet<Annotation> chunksToRemove = new HashSet<>();
        for (final Annotation chunk : chunks) {
          if (chunk.equals(foreignChunk)) {
            found = true;
            break;
          } else if (chunk.getType().equals(foreignChunk.getType())) {
            final int cb = chunk.getBegin();
            final int ce = chunk.getEnd();
            final int fb = foreignChunk.getBegin();
            final int fe = foreignChunk.getEnd();
            if ((cb <= fb) && (ce >= fe)) {
              found = true;
              break;
            } else if ((cb >= fb) && (ce <= fe)) {
              chunksToRemove.add(chunk);
            } else if ((cb >= fb && cb <= fe) || (ce >= fb && ce <= fe)) {
              found = true;
              break;
            }
          }
        }
        for (final Annotation chunkToRemove : chunksToRemove) {
          removeChunk(chunkToRemove);
        }
        if (!found) {
          addChunk(foreignChunk);
        }
      }
    }
  }

  /**
   * Returns all anotation types in AnnotationSet
   */
  public HashSet<String> getAnnotationTypes() {
    final HashSet<String> types = new HashSet<>();
    for (final Annotation an : chunks) {
      types.add(an.getType());
    }
    return types;
  }

  @Override
  public int hashCode() {
    return Objects.hash(chunks, sentence);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (getClass() != o.getClass()) {
      return false;
    }

    final AnnotationSet otherAnnSet = (AnnotationSet) o;

    return sentence.toString().equals(otherAnnSet.getSentence().toString()) && chunks.equals(otherAnnSet.chunkSet());
  }
}
