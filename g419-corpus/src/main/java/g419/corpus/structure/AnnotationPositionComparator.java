package g419.corpus.structure;

import java.util.Comparator;


/**
 * Porównywarka anotacji służąca do sortowania wg. pozycji w tekście
 *
 * @author Adam Kaczmarek
 */
public class AnnotationPositionComparator implements Comparator<Annotation> {

  /**
   * Anotacje są porównywane wg. pozycji w tekście, a następnie wg. długości
   */
  @Override
  public int compare(final Annotation ann1, final Annotation ann2) {
    // Różnica zdań
    final int sentDiff = ann1.getSentence().getOrd() - ann2.getSentence().getOrd();
    if (sentDiff != 0) {
      return sentDiff;
    }
    // Różnica pozycji wewnątrz zdania
    final int posDiff = ann1.getBegin() - ann2.getBegin();
    if (posDiff != 0) {
      return posDiff;
    }

    final int sizeDiff = ann1.getTokens().size() - ann2.getTokens().size();
    if (sizeDiff != 0) {
      return sizeDiff;
    }

    return ann1.getType().compareTo(ann2.getType());
  }

}
