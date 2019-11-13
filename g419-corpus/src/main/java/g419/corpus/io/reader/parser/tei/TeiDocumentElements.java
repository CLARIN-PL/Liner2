package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TeiDocumentElements {
  Map<String, Annotation> annotationMap = Maps.newHashMap();
  Map<String, Integer> tokensIdMap = Maps.newHashMap();
  Map<String, List<String>> elementsIdMap = Maps.newHashMap();
  List<Paragraph> paragraphs = Lists.newArrayList();
  final Map<String, String> headIds = Maps.newHashMap();
  List<Relation> relations = Lists.newArrayList();

  public List<Integer> getTokens(final String elementKey) {
    final String key = elementKey;
    if (tokensIdMap.containsKey(key)) {
      return Lists.newArrayList(tokensIdMap.get(key));
    } else if (elementsIdMap.containsKey(key)) {
      return elementsIdMap.get(key).stream()
          .map(this::getTokens)
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      LoggerFactory.getLogger(getClass()).warn("No element nor tokens for {}", key);
      return Lists.newArrayList();
    }
  }

  public Optional<Integer> getHeadToken(final String elementKey) {
    if (headIds.containsKey(elementKey)) {
      return getHeadToken(headIds.get(elementKey));
    } else if (annotationMap.containsKey(elementKey)) {
      return Optional.of(annotationMap.get(elementKey).getHead());
    } else {
      return getTokens(elementKey).stream().findFirst();
    }
  }

  public void addAnnotation(final String key, final Annotation an) {
    assert key.matches("^.+[#].+$");
    annotationMap.put(key, an);
  }
}
