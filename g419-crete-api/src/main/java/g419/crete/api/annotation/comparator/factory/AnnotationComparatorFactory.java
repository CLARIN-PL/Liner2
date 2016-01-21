package g419.crete.api.annotation.comparator.factory;

import g419.corpus.structure.Annotation;
import g419.crete.api.annotation.comparator.factory.item.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Created by akaczmarek on 14.12.15.
 */
public class AnnotationComparatorFactory {

    private HashMap<String, IAnnotationComparatorItem> comparators;
    private AnnotationComparatorFactory(){
        comparators = new HashMap<>();
        comparators.put("token_list_comparator_ccl", new AnnotationTokenListComparatorItem(false));
        comparators.put("token_list_comparator_tei", new AnnotationTokenListComparatorItem(true));
        comparators.put("head_comparator_ccl", new AnnotationHeadComparatorItem(false));
        comparators.put("head_comparator_tei", new AnnotationHeadComparatorItem(true));
        comparators.put("postion_comparator", new AnnotationPositionComparatorItem());
        comparators.put("exact_comparator", new AnnotationExactComparatorItem());
    }

    private static class FactoryHolder {
        private static final AnnotationComparatorFactory FACTORY = new AnnotationComparatorFactory();
    }
    public static AnnotationComparatorFactory getFactory(){
        return FactoryHolder.FACTORY;
    }

    public Comparator<Annotation> getComparator(String name) throws NoSuchElementException {
        IAnnotationComparatorItem comparator = comparators.get(name);
        if(comparator == null) throw new NoSuchElementException();
        return comparator.getAnnotationComparator();
    }

    public void register(String name, IAnnotationComparatorItem comparatorItem){
        comparators.put(name, comparatorItem);
    }

}
