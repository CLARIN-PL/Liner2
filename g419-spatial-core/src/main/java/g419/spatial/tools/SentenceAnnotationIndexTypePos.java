package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * Creates an index of annotation within a sentence.
 * The annotations are indexed by types and token range.
 */
public class SentenceAnnotationIndexTypePos {

    final private Map<String, Map<Integer, List<Annotation>>> index = Maps.newHashMap();
    final private Sentence sentence;

    public SentenceAnnotationIndexTypePos(final Sentence sentence){
        this.sentence = sentence;
        sentence.getChunks().stream().forEach(this::add);
        MapUtils.debugPrint(System.out, "myMap", index);
    }

    public void add(Annotation an) {
        Map<Integer, List<Annotation>> typeIndex = index.computeIfAbsent(an.getType(), t -> Maps.newHashMap());
        for ( Integer pos=an.getBegin(); pos<=an.getEnd(); pos++){
            typeIndex.computeIfAbsent(pos, p-> Lists.newArrayList()).add(an);
        }
    }

    public boolean hasAnnotationOfTypeAtPosition(String type, Integer pos){
        return index.containsKey(type) && index.get(type).containsKey(pos) && index.get(type).get(pos).size() > 0;
    }

    public List<Annotation> getAnnotationsOfTypeAtPosition(String type, Integer pos){
        return index.getOrDefault(type, Maps.newHashMap()).getOrDefault(pos, Lists.newArrayList());
    }

}
