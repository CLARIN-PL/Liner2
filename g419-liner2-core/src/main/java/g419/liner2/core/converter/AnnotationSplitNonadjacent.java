package g419.liner2.core.converter;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.List;
import java.util.TreeSet;

public class AnnotationSplitNonadjacent extends Converter {

  @Override
  public void apply(final Sentence sentence) {
    final List<Annotation> toRemove = Lists.newArrayList();
    final List<Annotation> toAdd = Lists.newArrayList();
    sentence.getChunks().forEach(an -> {
      final List<TreeSet<Integer>> parts = split(an);
      if (parts.size() > 1) {
        toRemove.add(an);
        parts.stream()
            .map(s -> new Annotation(s, an.getType(), an.getSentence()).withGroup(an.getGroup()))
            .forEach(toAdd::add);
      }
    });
    sentence.getChunks().removeAll(toRemove);
    sentence.getChunks().addAll(toAdd);
  }

  private List<TreeSet<Integer>> split(final Annotation annotation) {
    final List<TreeSet<Integer>> parts = Lists.newArrayList();
    TreeSet<Integer> lastSet = null;
    Integer lastIndex = null;

    for (final Integer n : Lists.newArrayList(annotation.getTokens())) {
      if (lastIndex != null && lastIndex + 1 < n) {
        parts.add(lastSet);
        lastSet = null;
      }
      if (lastSet == null) {
        lastSet = Sets.newTreeSet();
      }
      lastSet.add(n);
      lastIndex = n;
    }
    if (lastSet != null) {
      parts.add(lastSet);
    }
    return parts;
  }

}
