package liner2.features.annotations;

import liner2.structure.Annotation;
import liner2.structure.Sentence;
import liner2.structure.Token;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/5/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationFeatureClosestBase extends AnnotationSentenceFeature {

    private int distance;
    private String pos;
    private boolean searchForward;

    public AnnotationFeatureClosestBase(String pos, int distance){
        this.pos = pos;
        this.distance = Math.abs(distance);
        searchForward = distance > 0;

    }

    @Override
    public HashMap<Annotation, String> generate(Sentence sent, HashSet<Annotation> sentenceAnnotations) {
        HashMap<Annotation, String> features = new HashMap<Annotation, String>();
        int posIndex = sent.getAttributeIndex().getIndex("ctag");
        for(Annotation ann: sentenceAnnotations){
            List<Token> candidateTokens;
            if(searchForward)
                candidateTokens =  sent.getTokens().subList(ann.getEnd(), sent.getTokenNumber());
            else{
                candidateTokens = sent.getTokens().subList(0, ann.getBegin());
                Collections.reverse(candidateTokens);
            }
            int currentDistance = 0;
            for(Token tok: candidateTokens){
                String posVal = tok.getAttributeValue(posIndex).split(":")[0];
                if(posVal.equals(this.pos)){
                    currentDistance++;
                    if(currentDistance == distance){
                        features.put(ann, tok.getFirstValue());
                        break;
                    }
                }
            }
            if(!features.containsKey(ann))
                features.put(ann, "NULL");

        }
        return features;
    }

}
