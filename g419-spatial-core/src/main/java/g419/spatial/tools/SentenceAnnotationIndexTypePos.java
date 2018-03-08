package g419.spatial.tools;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import org.apache.commons.collections4.ListUtils;

import java.util.*;

/**
 * Creates an typeToPosToAnnIndex of annotation within a sentence.
 * The annotations are indexed by types and token range.
 */
public class SentenceAnnotationIndexTypePos {

    final private Map<String, Map<Integer, List<Annotation>>> typeToPosToAnnIndex = Maps.newHashMap();
    final private Map<Integer, List<Annotation>> posToAnnIndex = Maps.newHashMap();
    final private Sentence sentence;

    public SentenceAnnotationIndexTypePos(final Sentence sentence){
        this.sentence = sentence;
        sentence.getChunks().stream().forEach(this::add);
    }

    public void add(final Annotation an) {
        Map<Integer, List<Annotation>> typeIndex = typeToPosToAnnIndex.computeIfAbsent(an.getType(), t -> Maps.newHashMap());
        for ( Integer pos=an.getBegin(); pos<=an.getEnd(); pos++){
            typeIndex.computeIfAbsent(pos, p-> Lists.newArrayList()).add(an);
            posToAnnIndex.computeIfAbsent(pos, p->Lists.newArrayList()).add(an);
        }
    }

    public boolean hasAnnotationOfTypeAtPosition(final String type, final Integer pos){
        return typeToPosToAnnIndex.containsKey(type) && typeToPosToAnnIndex.get(type).getOrDefault(pos, Lists.newArrayList()).size()>0;
    }

    public List<Annotation> getAnnotationsOfTypeAtPos(final String type, final Integer pos){
        return typeToPosToAnnIndex.getOrDefault(type, Maps.newHashMap()).getOrDefault(pos, Lists.newArrayList());
    }

    /**
     * Return list of annotations at position pos.
     * @param pos
     * @return
     */
    public List<Annotation> getAtPos(final Integer pos){
        return posToAnnIndex.computeIfAbsent(pos, k->Lists.newArrayList());
    }

    public Annotation getLongestAtPos(final Integer pos){
        List<Annotation> ans = getAtPos(pos);
        if (ans.size()==0){
            return null;
        }
        sortAnnotationsLengthDescBeginAsc(ans);
        return ans.get(0);
    }

    /**
     * Return the longest annotation at position pos limited to annotations from set anns.
     * Annotations from anns which were not indexed will be ignored.
     * @param pos
     * @param ans
     * @return
     */
    public Annotation getLongestAtPosFromSet(Integer pos, List<Annotation> ans){
        List<Annotation> posAnnotations = getAtPos(pos);
        posAnnotations.retainAll(ans);
        sortAnnotationsLengthDescBeginAsc(ans);
        return posAnnotations.size() == 0 ? null : posAnnotations.get(0);
    }

    public  static void sortAnnotationsLengthDescBeginAsc(List<Annotation> ans){
        Collections.sort(ans, Comparator.comparing(Annotation::length, Comparator.reverseOrder()).thenComparing(Annotation::getBegin));
    }
}
