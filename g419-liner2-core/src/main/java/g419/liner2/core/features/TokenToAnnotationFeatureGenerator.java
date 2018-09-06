package g419.liner2.core.features;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.core.features.tokens.TokenFeature;
import g419.liner2.core.features.tokens.TokenInSentenceFeature;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michal on 10/22/14.
 */
public class TokenToAnnotationFeatureGenerator {

    private ArrayList<TokenFeature> tokenGenerators = new ArrayList<TokenFeature>();
    private ArrayList<TokenInSentenceFeature> sentenceGenerators = new ArrayList<TokenInSentenceFeature>();

    public TokenToAnnotationFeatureGenerator(TokenFeatureGenerator tokenFG){
        getGenerators(tokenFG);
    }

    public void mapFeatures(Sentence sentence, HashMap<Token, Annotation> annotatedTokens){
        for(Token token: annotatedTokens.keySet()) {
            Annotation ann = annotatedTokens.get(token);
            token.setAttributeValue("orth", ann.getText());
            token.setAttributeValue("base", ann.getBaseText());
            for(TokenFeature tokenFeat: tokenGenerators){
                token.setAttributeValue(tokenFeat.getName(), tokenFeat.generate(token, token.attrIdx));
            }
        }

        for(TokenInSentenceFeature sentenceFeat: sentenceGenerators) {
            sentenceFeat.generate(sentence);
        }
    }

    private boolean featureToGenerate(String feature){
        return feature.equals("pattern") || feature.startsWith("prefix") || feature.startsWith("suffix") || feature.startsWith("starts_with") || feature.startsWith("has");
    }

    private void getGenerators(TokenFeatureGenerator tokenFG){
        features:
        for(String feature: tokenFG.featureNames){
            if(featureToGenerate(feature)){
                for(TokenFeature f: tokenFG.tokenGenerators){
                    if(f.getName().equals(feature)){
                        this.tokenGenerators.add(f);
                        continue features;
                    }
                }
                for(TokenInSentenceFeature f: tokenFG.sentenceGenerators){
                    if(f.getName().equals(feature)){
                        this.sentenceGenerators.add(f);
                        continue features;
                    }
                }
            }
        }
    }
}
