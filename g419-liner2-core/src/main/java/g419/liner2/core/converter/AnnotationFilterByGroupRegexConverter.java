package g419.liner2.core.converter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnnotationFilterByGroupRegexConverter extends Converter {

  final private Pattern pattern;

  public AnnotationFilterByGroupRegexConverter(final Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public void apply(final Sentence sentence) {
    final List<Annotation> toRemove = sentence.getChunks().stream()
        .filter(an -> !pattern.matcher(an.getGroup()).find())
        .collect(Collectors.toList());
    sentence.getChunks().removeAll(toRemove);
  }
}
