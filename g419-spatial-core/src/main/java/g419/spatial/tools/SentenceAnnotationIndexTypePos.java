package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates an typeToPosToAnnIndex of annotation within a sentence.
 * The annotations are indexed by types and token range.
 */
public class SentenceAnnotationIndexTypePos {

    final private Map<String, Map<Integer, List<Annotation>>> typeToPosToAnnIndex = Maps.newHashMap();
    final private Map<Integer, List<Annotation>> posToAnnIndex = Maps.newHashMap();
    final private Sentence sentence;

    public SentenceAnnotationIndexTypePos(final Sentence sentence) {
        this.sentence = sentence;
        sentence.getChunks().stream().forEach(this::add);
    }

    public static void sortAnnotationsLengthDescBeginAsc(List<Annotation> ans) {
        Collections.sort(ans, Comparator.comparing(Annotation::length, Comparator.reverseOrder()).thenComparing(Annotation::getBegin));
    }

    public void add(final Annotation an) {
        final Map<Integer, List<Annotation>> typeIndex = typeToPosToAnnIndex.computeIfAbsent(an.getType(), t -> Maps.newHashMap());
        for (Integer pos = an.getBegin(); pos <= an.getEnd(); pos++) {
            typeIndex.computeIfAbsent(pos, p -> Lists.newArrayList()).add(an);
            posToAnnIndex.computeIfAbsent(pos, p -> Lists.newArrayList()).add(an);
        }
    }

    public boolean hasAnnotationOfTypeAtPosition(final String type, final Integer pos) {
        return typeToPosToAnnIndex.containsKey(type) && typeToPosToAnnIndex.get(type).getOrDefault(pos, Lists.newArrayList()).size() > 0;
    }

    public List<Annotation> getAnnotationsOfTypeAtPos(final String type, final Integer pos) {
        return typeToPosToAnnIndex.getOrDefault(type, Maps.newHashMap()).getOrDefault(pos, Lists.newArrayList());
    }

    /**
     * Return list of annotations at position pos.
     *
     * @param pos
     * @return
     */
    public List<Annotation> getAtPos(final Integer pos) {
        return posToAnnIndex.computeIfAbsent(pos, k -> Lists.newArrayList());
    }

    public Annotation getLongestAtPos(final Integer pos) {
        final List<Annotation> ans = getAtPos(pos);
        //LoggerFactory.getLogger(getClass()).debug(getIndexDump());
        //LoggerFactory.getLogger(getClass()).debug("" + pos + " " + ans);
        if (ans.size() == 0) {
            return null;
        }
        sortAnnotationsLengthDescBeginAsc(ans);
        return ans.get(0);
    }

    public String getIndexDump(){
        final StringJoiner sjo = new StringJoiner("\n");
        for (int i=0; i<sentence.getTokenNumber(); i++){
            final StringJoiner sj = new StringJoiner(" ");
            sj.add(String.format("[%2d]=%-15s => ", i, sentence.getTokens().get(i).getOrth()));
            posToAnnIndex.getOrDefault(i, Lists.newArrayList()).stream().forEach(a->sj.add(a.getType()));
            sjo.add(sj.toString());
        }
        return sjo.toString();
    }

    /**
     * Return the longest annotation at position pos limited to annotations from set anns.
     * Annotations from anns which were not indexed will be ignored.
     *
     * @param pos
     * @param ans
     * @return
     */
    public Annotation getLongestAtPosFromSet(final Integer pos, final Collection<Annotation> ans) {
        List<Annotation> posAnnotations = getAtPos(pos).stream().filter(a -> ans.contains(a)).collect(Collectors.toList());
        sortAnnotationsLengthDescBeginAsc(posAnnotations);
        return posAnnotations.size() == 0 ? null : posAnnotations.get(0);
    }

    public Annotation getFirstInRangeFromSet(final Integer startPos, final Integer endPos, final Collection<Annotation> ans){
        int i=startPos;
        Annotation found = null;
        while (found==null && i<=endPos){
            found = getLongestAtPosFromSet(i++,ans);
        }
        return found;
    }
}
