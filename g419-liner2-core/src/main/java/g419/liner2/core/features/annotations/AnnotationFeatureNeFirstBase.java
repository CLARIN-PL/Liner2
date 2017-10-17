package g419.liner2.core.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/5/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationFeatureNeFirstBase extends AnnotationSentenceFeature {

    private int distance;
    private String pos;
    private boolean searchForward;

    public AnnotationFeatureNeFirstBase(String pos, int distance){
        this.pos = pos;
        this.distance = Math.abs(distance);
        searchForward = distance > 0;

    }

    @Override
    public Map<Annotation, String> generate(Sentence sent, Set<Annotation> sentenceAnnotations) {
        Map<Annotation, String> features = new HashMap<Annotation, String>();
        int posIndex = sent.getAttributeIndex().getIndex("ctag");
        for(Annotation ann: sentenceAnnotations){
            List<Token> candidateTokens;
            candidateTokens =  new ArrayList<Token>(sent.getTokens().subList(ann.getBegin(), ann.getEnd()+1));
            if(!searchForward)
                Collections.reverse(candidateTokens);

            int currentDistance = 0;
            for(Token tok: candidateTokens){
                String posVal = tok.getAttributeValue(posIndex).split(":")[0];
                if(posVal.equals(this.pos)){
                    if(!searchForward){
                        currentDistance++;
                    }
                    if(currentDistance == distance){
                        features.put(ann, tok.getAttributeValue("base"));
                        break;
                    }
                    if(searchForward){
                        currentDistance++;
                    }
                }
            }
            if(!features.containsKey(ann))
                features.put(ann, "NULL");

        }
        return features;
    }

}
