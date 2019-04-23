package g419.liner2.core.converter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * Created by michal on 8/20/14.
 */
public class AnnotationFilterByTypeRegexConverter extends Converter {

  private Pattern pattern = null;

  public AnnotationFilterByTypeRegexConverter(final Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public void finish(final Document doc) {

  }

  @Override
  public void start(final Document doc) {

  }

  @Override
  public void apply(final Sentence sentence) {
    final LinkedHashSet<Annotation> sentenceAnnotations = sentence.getChunks();
    final LinkedHashSet<Annotation> toRemove = new LinkedHashSet<>();
    for (final Annotation ann : sentenceAnnotations) {
      if (!pattern.matcher(ann.getType()).find()) {
        toRemove.add(ann);
      }
    }
    for (final Annotation ann : toRemove) {
      sentenceAnnotations.remove(ann);
    }
  }
}
