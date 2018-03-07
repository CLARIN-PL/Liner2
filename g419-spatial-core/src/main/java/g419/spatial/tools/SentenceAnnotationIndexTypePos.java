package g419.spatial.tools;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
        //MapUtils.debugPrint(System.out, "myMap", typeToPosToAnnIndex);
    }

    public void add(Annotation an) {
        Map<Integer, List<Annotation>> typeIndex = typeToPosToAnnIndex.computeIfAbsent(an.getType(), t -> Maps.newHashMap());
        for ( Integer pos=an.getBegin(); pos<=an.getEnd(); pos++){
            typeIndex.computeIfAbsent(pos, p-> Lists.newArrayList()).add(an);
            posToAnnIndex.computeIfAbsent(pos, p->Lists.newArrayList()).add(an);
        }
    }

    public boolean hasAnnotationOfTypeAtPosition(String type, Integer pos){
        return typeToPosToAnnIndex.containsKey(type) && typeToPosToAnnIndex.get(type).containsKey(pos) && typeToPosToAnnIndex.get(type).get(pos).size() > 0;
    }

    public List<Annotation> getAnnotationsOfTypeAtPosition(String type, Integer pos){
        return typeToPosToAnnIndex.getOrDefault(type, Maps.newHashMap()).getOrDefault(pos, Lists.newArrayList());
    }

    public Annotation getLongestAtPos(Integer pos){
        List<Annotation> ans = posToAnnIndex.get(pos);
        if (ans==null){
            return null;
        } else {
            Collections.sort(ans, Comparator.comparing(Annotation::length, Comparator.reverseOrder()).thenComparing(Annotation::getBegin));
            return ans.get(0);
        }
    }
}
