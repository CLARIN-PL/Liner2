package g419.liner2.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.List;

/**
 * Created by michal on 10/27/14.
 */
public class AnnotationFeatureHeadValue extends AnnotationAtomicFeature{

    String sourceFeat;

    public AnnotationFeatureHeadValue(String sourceFeat){
        this.sourceFeat = sourceFeat;
    }

    @Override
    public String generate(Annotation an) {
        return findHead(an).getAttributeValue(sourceFeat);
    }

    private Token findHead(Annotation ann){
        Sentence sentence = ann.getSentence();
        List<Token> tokens = sentence.getTokens();
        TokenAttributeIndex index = sentence.getAttributeIndex();
        for(Token tok: tokens.subList(ann.getBegin(), ann.getEnd() + 1)){
            String tokClass = tok.getAttributeValue(index.getIndex("class"));
            if(tokClass != null && tokClass.equals("subst")){
                return tok;
            }
        }
        return tokens.get(ann.getBegin());
    }
}
