package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class NkjpSyntacticChunks {

  static public Pattern annotationsPrep = Pattern.compile("^Prep.+$");
  static public Pattern annotationsNg = Pattern.compile("^NG.*$");

  static public void splitPrepNg(final Sentence sentence) {
    final Map<Integer, List<Annotation>> mapTokenIdToAnnotations = createTokenToAnnotationMap(sentence);
    final Set<Annotation> prepNg = sentence.getAnnotations(NkjpSyntacticChunks.annotationsPrep);
    prepNg.forEach(an -> splitPrepNgIfNeeded(sentence, an, mapTokenIdToAnnotations));
  }

  private static void splitPrepNgIfNeeded(final Sentence sentence,
                                          final Annotation an,
                                          final Map<Integer, List<Annotation>> mapTokenIdToAnnotations) {
    if (!mapTokenIdToAnnotations.containsKey(an.getBegin() + 1)) {
      if (an.getTokenCount() > 1) {
        final Annotation ani = new Annotation(an.getBegin() + 1,
                an.getEnd(), an.getType().substring(4), an.getSentence());
        if (ani.getTokens().contains(an.getHead())) {
          ani.setHead(an.getHead());
        } else {
          ani.assignHead(true);
        }
        sentence.addChunk(ani);
      }
    } else {
      Integer newNgStart = null;
      for (int i = an.getBegin() + 1; i <= an.getEnd(); i++) {
        if (mapTokenIdToAnnotations.get(i) == null) {
          if (newNgStart == null) {
            newNgStart = i;
          }
        } else if (newNgStart != null) {
          sentence.addChunk(new Annotation(newNgStart, i - 1, "NG", sentence));
          newNgStart = null;
        }
      }
      if (newNgStart != null) {
        sentence.addChunk(new Annotation(newNgStart, an.getEnd(), "NG", sentence));
      }
    }
  }

  static Map<Integer, List<Annotation>> createTokenToAnnotationMap(final Sentence sentence) {
    final Map<Integer, List<Annotation>> mapTokenIdToAnnotations = Maps.newHashMap();
    for (final Annotation an : sentence.getAnnotations(NkjpSyntacticChunks.annotationsNg)) {
      for (int i = an.getBegin(); i <= an.getEnd(); i++) {
        mapTokenIdToAnnotations.computeIfAbsent(i, o -> Lists.newLinkedList()).add(an);
      }
    }
    return mapTokenIdToAnnotations;
  }

}
