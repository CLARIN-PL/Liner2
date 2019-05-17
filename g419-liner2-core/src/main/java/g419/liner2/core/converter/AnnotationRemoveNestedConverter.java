package g419.liner2.core.converter;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class AnnotationRemoveNestedConverter extends Converter {

  @Override
  public void apply(final Sentence sentence) {
    final LinkedHashSet<Annotation> sentenceAnnotations = sentence.getChunks();
    final HashSet<Annotation> toRemove = new HashSet<>();
    for (final String type : getTypes(sentenceAnnotations)) {
      final HashSet<Annotation> oneType = getOneType(type, sentenceAnnotations);
      for (final Annotation candidate : oneType) {
        for (final Annotation ann : oneType) {
          if (ann != candidate && ann.getTokens().containsAll(candidate.getTokens())) {
            toRemove.add(candidate);
            break;
          }

        }
      }
    }
    toRemove.stream()
        .filter(an -> overlaps(an, sentenceAnnotations))
        .forEach(sentenceAnnotations::remove);
  }

  /**
   * Checks if given annotation is nested with another annotation of the same type.
   *
   * @param ann -- annotation to check
   * @param set -- set of annoations
   * @return
   */
  private boolean overlaps(final Annotation ann, final Set<Annotation> set) {
    for (final Annotation ann2 : set) {
      if (ann != ann2
          && ann.getType().equals(ann2.getType())
          && ann2.getTokens().containsAll(ann.getTokens())) {
        return true;
      }
    }
    return false;
  }

  private HashSet<Annotation> getOneType(final String type, final LinkedHashSet<Annotation> as) {
    final HashSet<Annotation> oneType = new HashSet<>();
    for (final Annotation ann : as) {
      if (ann.getType().equals(type)) {
        oneType.add(ann);
      }
    }
    return oneType;
  }

  private HashSet<String> getTypes(final LinkedHashSet<Annotation> as) {
    final HashSet<String> types = new HashSet<>();
    for (final Annotation ann : as) {
      types.add(ann.getType());
    }
    return types;
  }
}
