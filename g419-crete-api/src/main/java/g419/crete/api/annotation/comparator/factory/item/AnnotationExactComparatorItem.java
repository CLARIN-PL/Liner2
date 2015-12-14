package g419.crete.api.annotation.comparator.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationExactMatchComparator;
import g419.crete.api.annotation.comparator.factory.IAnnotationComparatorItem;

import java.util.Comparator;

/**
 * Created by akaczmarek on 14.12.15.
 */
public class AnnotationExactComparatorItem implements IAnnotationComparatorItem {

    @Override
    public Comparator<Annotation> getAnnotationComparator() {
        return new AnnotationExactMatchComparator();
    }
}
