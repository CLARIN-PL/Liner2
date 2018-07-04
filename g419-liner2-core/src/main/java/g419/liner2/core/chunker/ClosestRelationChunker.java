package g419.liner2.core.chunker;

import g419.corpus.structure.*;

import java.util.*;
import java.util.regex.Pattern;


/**
 * @author Jan Kocoń
 * 
 */

public class ClosestRelationChunker extends Chunker {

    private Pattern annotationFromPattern = null;
    private Pattern annotationToPattern = null;


    public ClosestRelationChunker(String annotationFromPattern, String annotationToPattern) {
        this.annotationFromPattern = Pattern.compile(annotationFromPattern);
        this.annotationToPattern = Pattern.compile(annotationToPattern);

	}


    @Override
    public Map<Sentence, AnnotationSet> chunk(Document ps) {
        HashMap<Sentence, AnnotationSet> out = new HashMap<>();
        return out;
    }

    public int getMinimalDistance(Annotation a1, Annotation a2) {
        return Collections.min(Arrays.asList(
                Math.abs(a1.getBegin() - a2.getBegin()),
                Math.abs(a1.getBegin() - a2.getEnd()),
                Math.abs(a1.getEnd() - a2.getBegin()),
                Math.abs(a1.getEnd() - a2.getEnd())
                ));
    }

    @Override
    public void chunkInPlace(Document ps){
        for ( Paragraph paragraph : ps.getParagraphs() )
            for (Sentence sentence : paragraph.getSentences()) {
                LinkedHashSet<Annotation> annotationsFrom = sentence.getAnnotations(this.annotationFromPattern);
                LinkedHashSet<Annotation> annotationsTo = sentence.getAnnotations(this.annotationToPattern);

                for (Annotation annotationFrom : annotationsFrom) {
                    Annotation closestAnnotationTo = null;
                    int distance = Integer.MAX_VALUE;

                    for (Annotation annotationTo : annotationsTo){
                        int currentDistance = getMinimalDistance(annotationFrom, annotationTo);
                        if (currentDistance < distance) {
                            distance = currentDistance;
                            closestAnnotationTo = annotationTo;
                        }
                    }
                    if (closestAnnotationTo != null){
                        ps.addRelation(new Relation(annotationFrom, closestAnnotationTo, "modality"));
                    }


                }

            }
    }

}
