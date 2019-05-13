package g419.liner2.core.chunker;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.PolemLemmatizer;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * @author Michał Marcińczuk
 */

public class PolemChunker extends Chunker {

  final List<Pattern> annotationPatterns;
  final PolemLemmatizer polem = new PolemLemmatizer();
  final private Chunker baseChunker;

  public PolemChunker(Chunker baseChunker, List<Pattern> annotationPatterns) {
    this.baseChunker = baseChunker;
    this.annotationPatterns = annotationPatterns;
  }

  @Override
  public void prepare(Document ps) {

  }

  @Override
  public Map<Sentence, AnnotationSet> chunk(Document ps) {
    Map<Sentence, AnnotationSet> chunkings = this.baseChunker.chunk(ps);
    chunkings.values().stream().forEach(set -> set.chunkSet().forEach(an -> polem.lemmatize(an)));
    return chunkings;
  }

}
