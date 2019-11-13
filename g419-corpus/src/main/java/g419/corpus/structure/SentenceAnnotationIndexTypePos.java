package g419.corpus.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.vavr.control.Option;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Creates an typeToPosToAnnIndex of annotation within a sentence.
 * The annotations are indexed by types and token range.
 */
public class SentenceAnnotationIndexTypePos {

  final private Map<String, Map<Integer, List<Annotation>>> typeToPosToAnnIndex = Maps.newHashMap();
  final private Map<Integer, List<Annotation>> posToAnnIndex = Maps.newHashMap();
  final private Sentence sentence;

  public SentenceAnnotationIndexTypePos(final Sentence sentence) {
    this.sentence = sentence;
    sentence.getChunks().stream().forEach(this::add);
  }

  public Sentence getSentence() {
    return sentence;
  }

  public static void sortAnnotationsLengthDescBeginAsc(final List<Annotation> ans) {
    Collections.sort(ans, Comparator.comparing(Annotation::length, Comparator.reverseOrder()).thenComparing(Annotation::getBegin));
  }

  public void add(final Annotation an) {
    final Map<Integer, List<Annotation>> typeIndex = typeToPosToAnnIndex.computeIfAbsent(an.getType(), t -> Maps.newHashMap());
    for (Integer pos = an.getBegin(); pos <= an.getEnd(); pos++) {
      typeIndex.computeIfAbsent(pos, p -> Lists.newArrayList()).add(an);
      posToAnnIndex.computeIfAbsent(pos, p -> Lists.newArrayList()).add(an);
    }
  }

  public boolean hasAnnotationOfTypeAtPosition(final String type, final Integer pos) {
    return typeToPosToAnnIndex.containsKey(type) && typeToPosToAnnIndex.get(type).getOrDefault(pos, Lists.newArrayList()).size() > 0;
  }

  public Option<Annotation> getLongestOfTypeAtPos(final String type, final Integer pos) {
    return Option.ofOptional(getAnnotationsOfTypeAtPos(type, pos).stream()
        .sorted(Comparator.comparing(Annotation::length).reversed())
        .findFirst());
  }

  public Option<Annotation> getLongestOfTypeAtPos(final Pattern typePattern, final Integer pos) {
    return Option.ofOptional(getAnnotationsOfTypeAtPos(typePattern, pos).stream()
        .sorted(Comparator.comparing(Annotation::length).reversed())
        .findFirst());
  }

  public List<Annotation> getAnnotationsAtPos(final Integer pos) {
    return posToAnnIndex.getOrDefault(pos, Lists.newArrayList());
  }

  public List<Annotation> getAnnotationsStartingFrom(final Integer position) {
    return getAnnotationsAtPos(position).stream()
        .filter(an -> an.getBegin() == position)
        .collect(Collectors.toList());
  }

  public List<Annotation> getAnnotationsOfTypeAtPos(final String type, final Integer pos) {
    return typeToPosToAnnIndex.getOrDefault(type, Maps.newHashMap()).getOrDefault(pos, Lists.newArrayList());
  }

  public List<Annotation> getAnnotationsOfTypeAtPos(final Pattern typePattern, final Integer pos) {
    return posToAnnIndex.computeIfAbsent(pos, n -> Lists.newArrayList()).stream()
        .filter(an -> typePattern.matcher(an.getType()).find())
        .collect(Collectors.toList());
  }

  /**
   * Return list of annotations at position pos.
   *
   * @param pos
   * @return
   */
  public List<Annotation> getAtPos(final Integer pos) {
    return posToAnnIndex.computeIfAbsent(pos, k -> Lists.newArrayList());
  }

  public Annotation getLongestAtPos(final Integer pos) {
    final List<Annotation> ans = getAtPos(pos);
    if (ans.size() == 0) {
      return null;
    }
    sortAnnotationsLengthDescBeginAsc(ans);
    return ans.get(0);
  }

  public Option<Annotation> getAnnotationStartingFrom(final Integer position) {
    final List<Annotation> list = getAnnotationsAtPos(position).stream()
        .filter(an -> an.getBegin() == position)
        .collect(Collectors.toList());
    return Option.of(list.size() == 0 ? null : list.get(0));
  }

  public Option<Annotation> getAnnotationOfTypeStartingFrom(final String type, final Integer position) {
    final List<Annotation> list = getAnnotationsOfTypeAtPos(type, position).stream()
        .filter(an -> an.getBegin() == position)
        .collect(Collectors.toList());
    return Option.of(list.size() == 0 ? null : list.get(0));
  }

  public Option<Annotation> getAnnotationOfTypeStartingFrom(final Pattern typePattern, final Integer position) {
    final List<Annotation> list = getAnnotationsOfTypeAtPos(typePattern, position).stream()
        .filter(an -> an.getBegin() == position)
        .collect(Collectors.toList());
    return Option.of(list.size() == 0 ? null : list.get(0));
  }

  /**
   * Return the longest annotation at position pos limited to annotations from set anns.
   * Annotations from anns which were not indexed will be ignored.
   *
   * @param pos
   * @param ans
   * @return
   */
  public Annotation getLongestAtPosFromSet(final Integer pos, final Collection<Annotation> ans) {
    final List<Annotation> posAnnotations = getAtPos(pos).stream().filter(a -> ans.contains(a)).collect(Collectors.toList());
    sortAnnotationsLengthDescBeginAsc(posAnnotations);
    return posAnnotations.size() == 0 ? null : posAnnotations.get(0);
  }

  public Annotation getFirstInRangeFromSet(final Integer startPos, final Integer endPos, final Collection<Annotation> ans) {
    int i = startPos;
    Annotation found = null;
    while (found == null && i <= endPos) {
      found = getLongestAtPosFromSet(i++, ans);
    }
    return found;
  }
}
