package g419.corpus.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SentencePrinter {

  private final Sentence sentence;

  public SentencePrinter(final Sentence sentence) {
    this.sentence = sentence;
  }

  public List<String> getLinesWithAnnotationsByGroup(final String group) {
    final List<Map<Integer, Annotation>> maps = Lists.newArrayList(Maps.newHashMap());
    sentence.getChunks().stream()
        .filter(an -> Objects.equals(an.getGroup(), group))
        .sorted((a1, a2) -> Integer.compare(a2.getTokenCount(), a1.getTokenCount()))
        .forEach(an -> {
          final Map<Integer, Annotation> map = getAnnotationRow(maps, an);
          for (final Integer n : an.getTokens()) {
            map.put(n, an);
          }
        });
    return maps.stream().map(row -> {
      final StringBuilder line = new StringBuilder();
      int length = 0;
      String lastType = "";
      for (int i = 0; i < sentence.getTokenNumber(); i++) {
        final Annotation an = row.get(i);
        final int tokenLength = sentence.getTokens().get(i).getOrth().length();
        if (an == null) {
          lastType = "";
          if (line.length() < length + tokenLength) {
            line.append(StringUtils.repeat(" ", length + tokenLength - line.length()));
          }
        } else if (an.getType().equals(lastType)) {
          if (line.length() < length + tokenLength) {
            line.append(StringUtils.repeat("_", length + tokenLength - line.length()));
          }
        } else {
          if (line.length() < length) {
            line.append(StringUtils.repeat(" ", length - line.length()));
          }
          line.append(an.getType());
          lastType = an.getType();
        }
        length += tokenLength + (sentence.getTokens().get(i).getNoSpaceAfter() ? 0 : 1);
      }
      return line.toString();
    }).collect(Collectors.toList());
  }

  private Map<Integer, Annotation> getAnnotationRow(final List<Map<Integer, Annotation>> maps, final Annotation an) {
    for (final Map<Integer, Annotation> row : maps) {
      if (doesAnnotationFit(an, row)) {
        return row;
      }
    }
    final Map<Integer, Annotation> row = Maps.newHashMap();
    maps.add(row);
    return row;
  }

  private boolean doesAnnotationFit(final Annotation an, final Map<Integer, Annotation> map) {
    final Set<Integer> indices = Sets.newHashSet(an.getTokens());
    indices.retainAll(map.keySet());
    return indices.size() == 0;
  }
}
