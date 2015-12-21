package g419.crete.api.annotation.comparator.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationHeadComparator;

import java.util.Comparator;

/**
 * Created by akaczmarek on 14.12.15.
 */
public class AnnotationHeadComparatorItem implements IAnnotationComparatorItem {

    private boolean tei = false;

    public AnnotationHeadComparatorItem(boolean tei) {
        this.tei = tei;
    }

    @Override
    public Comparator<Annotation> getAnnotationComparator() {
        return new AnnotationHeadComparator(!tei);
    }
}
