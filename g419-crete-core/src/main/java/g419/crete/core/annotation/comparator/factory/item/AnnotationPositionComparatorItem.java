package g419.crete.core.annotation.comparator.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationPositionComparator;

import java.util.Comparator;

/**
 * Created by akaczmarek on 14.12.15.
 */
public class AnnotationPositionComparatorItem implements IAnnotationComparatorItem {
    @Override
    public Comparator<Annotation> getAnnotationComparator() {
        return new AnnotationPositionComparator();
    }
}
