package g419.spatial.pattern;

import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class SentencePatternResult {

  final TreeSet<Integer> matchedTokens = Sets.newTreeSet();
  final LinkedHashSet<Annotation> matchedAnnotations = Sets.newLinkedHashSet();

  public boolean matched() {
    return matchedTokens.size() > 0;
  }

  public Integer getMatchEnd() {
    return matchedTokens.last();
  }

  public void addToken(final int tokenIndex) {
    matchedTokens.add(tokenIndex);
  }

  public void addTokes(final int beginIndex, final int endIndex) {
    IntStream.rangeClosed(beginIndex, endIndex).forEach(matchedTokens::add);
  }

  public void addTokes(final Collection<Integer> tokens) {
    matchedTokens.addAll(tokens);
  }

  public TreeSet<Integer> getTokens() {
    return matchedTokens;
  }

  public LinkedHashSet<Annotation> getAnnotations() {
    return matchedAnnotations;
  }

  public void addAnnotation(final Annotation annotation) {
    matchedAnnotations.add(annotation);
    matchedTokens.addAll(annotation.getTokens());
  }

  public void mergeWith(final SentencePatternResult result) {
    addTokes(result.getTokens());
    result.getAnnotations().forEach(this::addAnnotation);
  }
}
