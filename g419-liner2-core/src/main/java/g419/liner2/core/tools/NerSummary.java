package g419.liner2.core.tools;

import com.google.common.collect.Lists;
import g419.corpus.HasLogger;
import g419.corpus.structure.Annotation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NerSummary implements HasLogger {

  @Data
  @AllArgsConstructor
  public class AnnotationGroup {
    private String name;
    private String type;
    private int score;
    private List<Annotation> annotations;
  }

  final Function<Annotation, String> keyGenerator = Annotation::getLemmaOrText;
  private final List<Annotation> annotations = Lists.newArrayList();

  public void addAll(final Collection<Annotation> annotations) {
    this.annotations.addAll(annotations);
  }

  public List<AnnotationGroup> getGroups() {
    return annotations.stream()
        .collect(Collectors.groupingBy(keyGenerator))
        .entrySet().stream()
        .map(e -> new AnnotationGroup(e.getKey(), e.getValue().get(0).getType(), e.getValue().size(), e.getValue()))
        .collect(Collectors.toList());
  }

}
