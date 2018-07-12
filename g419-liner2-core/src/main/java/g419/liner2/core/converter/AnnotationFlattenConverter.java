package g419.liner2.core.converter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by michal on 9/17/14.
 */
public class AnnotationFlattenConverter extends Converter {

    ArrayList<String> categories;
    private final Comparator<Annotation> flattenConparator;

    public AnnotationFlattenConverter(final ArrayList<String> cats) {
        categories = cats;

        flattenConparator = new Comparator<Annotation>() {
            @Override
            public int compare(final Annotation a, final Annotation b) {
                if (a == b || Collections.disjoint(a.getTokens(), b.getTokens())) {
                    return 0;
                } else {
                    if (a.getTokens().size() == b.getTokens().size()) {
                        return Integer.signum(categories.indexOf(a.getType()) - categories.indexOf(b.getType()));
                    }
                    return Integer.signum(b.getTokens().size() - a.getTokens().size());
                }
            }
        };
    }

    @Override
    public void apply(final Sentence sentence) {
        final ArrayList<Annotation> toFlatten = new ArrayList<>();
        for (final Annotation ann : sentence.getChunks()) {
            if (categories.contains(ann.getType())) {
                toFlatten.add(ann);
            }
        }
        for (final Annotation annToRemove : flatten(toFlatten)) {
            sentence.getChunks().remove(annToRemove);
        }
    }

    private HashSet<Annotation> flatten(final ArrayList<Annotation> toFlatten) {
        final HashSet<Annotation> toRemove = new HashSet<>();
        for (final Annotation ann : toFlatten) {
            for (final Annotation candidate : toFlatten) {
                if (flattenConparator.compare(ann, candidate) == -1) {
                    toRemove.add(candidate);
                }
            }
        }
        return toRemove;
    }
}
