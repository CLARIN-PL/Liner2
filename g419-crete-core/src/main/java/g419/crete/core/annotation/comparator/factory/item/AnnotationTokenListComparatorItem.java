package g419.crete.core.annotation.comparator.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationTokenListComparator;

import java.util.Comparator;

/**
 * Created by akaczmarek on 14.12.15.
 */
public class AnnotationTokenListComparatorItem implements IAnnotationComparatorItem {

    private boolean tei = false;

    public AnnotationTokenListComparatorItem(boolean tei) {
        this.tei = tei;
    }

    @Override
    public Comparator<Annotation> getAnnotationComparator() {
        return new AnnotationTokenListComparator(!tei);
    }
}
