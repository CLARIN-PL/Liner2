package g419.liner2.api.features.annotations;


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
    public HashMap<Annotation, String> generate(Sentence sent, LinkedHashSet<Annotation> sentenceAnnotations) {
        HashMap<Annotation, String> features = new HashMap<Annotation, String>();
        if(!sentenceAnnotations.isEmpty()){
            System.out.println("DISTANCE: " + distance + "FORWARD: " + searchForward);
            for(Token t: sent.getTokens()){
                System.out.println(t.getAttributeValue("orth") + " | " + t.getAttributeValue("base") + " | " + t.getAttributeValue("ctag"));
            }
        }
        int posIndex = sent.getAttributeIndex().getIndex("ctag");
        for(Annotation ann: sentenceAnnotations){
            System.out.println(ann.getText());
            List<Token> candidateTokens;
            if(searchForward) {
                candidateTokens =  new ArrayList<Token>(sent.getTokens().subList(ann.getEnd(), sent.getTokenNumber()));
            }
            else{
                candidateTokens = new ArrayList<Token>(sent.getTokens().subList(0, ann.getBegin()));
                Collections.reverse(candidateTokens);
            }
            int currentDistance = 0;
            for(Token tok: candidateTokens){
                String posVal = tok.getAttributeValue(posIndex).split(":")[0];
                if(posVal.equals(this.pos)){
                    if(!searchForward){
                        currentDistance++;
                    }
                    if(currentDistance == distance){
                        System.out.println("CHOSEN: " + tok.getAttributeValue("base"));
                        features.put(ann, tok.getAttributeValue("base"));
                        break;
                    }
                    if(searchForward){
                        currentDistance++;
                    }
                }
            }
            if(!features.containsKey(ann)) {
                features.put(ann, "NULL");
                System.out.println("CHOSEN: NOT FOUND");
            }

        }
        if(!sentenceAnnotations.isEmpty()){
            System.out.println("------------------");
        }
        return features;
    }

}
