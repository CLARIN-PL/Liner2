package g419.corpus.structure;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Creates an typeToPosToAnnIndex of annotation within a sentence.
 * The annotations are indexed by types and token range.
 */
public class DocumentAnnotationIndexTypePos {

  Map<Sentence, SentenceAnnotationIndexTypePos> sentenceIndices = Maps.newHashMap();

  public DocumentAnnotationIndexTypePos(final Document document) {
    for (final Sentence sentence : document.getSentences()) {
      sentenceIndices.put(sentence, new SentenceAnnotationIndexTypePos(sentence));
    }
  }

  public List<Annotation> getAnnotationsOfTypeAtHeadPos(final Annotation an, final String type) {
    return sentenceIndices.get(an.getSentence()).getAnnotationsOfTypeAtPos(type, an.getHead());
  }

  public List<Annotation> getAnnotationsOfTypeAtHeadPos(final Annotation an, final Pattern type) {
    return sentenceIndices.get(an.getSentence()).getAnnotationsOfTypeAtPos(type, an.getHead());
  }

}
