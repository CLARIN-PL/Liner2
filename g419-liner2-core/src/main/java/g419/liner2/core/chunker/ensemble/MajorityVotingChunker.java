package g419.liner2.core.chunker.ensemble;

import g419.corpus.structure.*;
import g419.liner2.core.chunker.Chunker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class MajorityVotingChunker extends Chunker {

  ArrayList<Chunker> chunkers;

  public MajorityVotingChunker(final ArrayList<Chunker> chunkers) {
    this.chunkers = chunkers;
  }

  public AnnotationSet voting(final Sentence sentence, final ArrayList<AnnotationSet> chunkings) {
    final AnnotationSet resultChunking = new AnnotationSet(sentence);

    final Hashtable<Annotation, Integer> votes = new Hashtable<>();

    for (final AnnotationSet chunking : chunkings) {
      for (final Annotation chunk : chunking.chunkSet()) {
        boolean found = false;
        for (final Annotation key : votes.keySet()) {
          if (chunk.equals(key)) {
            found = true;
            votes.put(key, votes.get(key) + 1);
            break;
          }
        }
        if (!found) {
          votes.put(chunk, 1);
        }
      }
    }

    final int majority = chunkers.size() / 2 + chunkers.size() % 2;
    for (final Annotation chunk : votes.keySet()) {
      if (votes.get(chunk) >= majority) {
        resultChunking.addChunk(chunk);
      }
    }

    return resultChunking;
  }

  @Override
  public Map<Sentence, AnnotationSet> chunk(final Document ps) {

    final Map<Sentence, AnnotationSet> chunkings = new HashMap<>();
    final Map<Sentence, ArrayList<AnnotationSet>> sentenceChunkings = new HashMap<>();

    for (final Paragraph paragraph : ps.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        sentenceChunkings.put(sentence, new ArrayList<>());
      }
    }

    for (final Chunker chunker : chunkers) {
      final Map<Sentence, AnnotationSet> chunkingsThis = chunker.chunk(ps);
      for (final Sentence sentence : chunkingsThis.keySet()) {
        sentenceChunkings.get(sentence).add(chunkingsThis.get(sentence));
      }
    }

    for (final Sentence sentence : sentenceChunkings.keySet()) {
      chunkings.put(sentence, voting(sentence, sentenceChunkings.get(sentence)));
    }

    return chunkings;
  }
}
