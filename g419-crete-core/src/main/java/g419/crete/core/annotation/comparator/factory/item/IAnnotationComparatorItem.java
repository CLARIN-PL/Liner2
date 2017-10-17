package g419.crete.core.annotation.comparator.factory.item;

import g419.corpus.structure.Annotation;

import java.util.Comparator;

/**
 * Created by akaczmarek on 14.12.15.
 */
public interface IAnnotationComparatorItem {
    public Comparator<Annotation> getAnnotationComparator();
}
