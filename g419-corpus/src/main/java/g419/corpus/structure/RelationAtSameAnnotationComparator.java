package g419.corpus.structure;

import java.util.Comparator;

/**
 * Porównywarka relacji, służąca do sortowania relacji w zbiorach relacji przypisanych do tej samej anotacji (vide RelationSet)
 *
 * @author Adam Kaczmarek
 */
public class RelationAtSameAnnotationComparator implements Comparator<Relation> {

  private final AnnotationPositionComparator annotationComparator = new AnnotationPositionComparator();

  @Override
  public int compare(final Relation r1, final Relation r2) {
    if (r1.getAnnotationFrom().equals(r2.getAnnotationFrom())) {
      return annotationComparator.compare(r1.getAnnotationTo(), r2.getAnnotationTo());
    } else {
      return annotationComparator.compare(r1.getAnnotationFrom(), r2.getAnnotationFrom());
    }
  }


}
