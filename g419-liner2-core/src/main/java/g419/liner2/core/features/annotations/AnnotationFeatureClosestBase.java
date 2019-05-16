package g419.liner2.core.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureClosestBase extends AnnotationAtomicFeature {

  private int distance;
  private String pos;
  private boolean searchForward;

  /**
   * @param pos    Part of speech to filter the tokens.
   * @param offset Token offset from the annotation. Only tokens matching the POST are counted.
   */
  public AnnotationFeatureClosestBase(String pos, int offset) {
    this.pos = pos;
    this.distance = Math.abs(offset);
    searchForward = offset > 0;
  }

  @Override
  public String generate(Annotation an) {
    String value = "NULL";
    List<Token> candidateTokens = null;
    Sentence sent = an.getSentence();
    int posIndex = sent.getAttributeIndex().getIndex("ctag");
    if (searchForward) {
      candidateTokens = new ArrayList<Token>(sent.getTokens().subList(an.getEnd(), sent.getTokenNumber()));
    } else {
      candidateTokens = new ArrayList<Token>(sent.getTokens().subList(0, an.getBegin()));
      Collections.reverse(candidateTokens);
    }
    int currentDistance = 0;
    for (Token tok : candidateTokens) {
      String posVal = tok.getAttributeValue(posIndex).split(":")[0];
      if (posVal.equals(this.pos)) {
        if (!searchForward) {
          currentDistance++;
        }
        if (currentDistance == distance) {
          value = tok.getAttributeValue("base");
          break;
        }
        if (searchForward) {
          currentDistance++;
        }
      }
    }
    return value;
  }

//    @Override
//    public Map<Annotation, String> generate(Sentence sent, Set<Annotation> sentenceAnnotations) {
//        Map<Annotation, String> features = new HashMap<Annotation, String>();
//        int posIndex = sent.getAttributeIndex().getIndex("ctag");
//        for(Annotation ann: sentenceAnnotations){
//            List<Token> candidateTokens;
//            if(searchForward) {
//                candidateTokens =  new ArrayList<Token>(sent.getTokens().subList(ann.getEnd(), sent.getTokenNumber()));
//            }
//            else{
//                candidateTokens = new ArrayList<Token>(sent.getTokens().subList(0, ann.getBegin()));
//                Collections.reverse(candidateTokens);
//            }
//            int currentDistance = 0;
//            for(Token tok: candidateTokens){
//                String posVal = tok.getAttributeValue(posIndex).split(":")[0];
//                if(posVal.equals(this.pos)){
//                    if(!searchForward){
//                        currentDistance++;
//                    }
//                    if(currentDistance == distance){
//                        features.put(ann, tok.getAttributeValue("base"));
//                        break;
//                    }
//                    if(searchForward){
//                        currentDistance++;
//                    }
//                }
//            }
//            if(!features.containsKey(ann)) {
//                features.put(ann, "NULL");
//            }
//        }
//        return features;
//    }


}
