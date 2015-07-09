package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

/**
 * Created by michal on 5/7/15.
 */
public class TestFeature extends TokenInSentenceFeature {

    String sourceFeature, operator, value;

    public TestFeature(String name, String sourceFeature, String operator, String value){
        super(name);
        this.sourceFeature = sourceFeature;
        this.operator = operator;
        this.value = value;

    }
    public String generate(Token token) {
        String featureVal = token.getAttributeValue(sourceFeature);
        if(operator.equals("equal")){
            return featureVal == null || !featureVal.equals(value) ? "0" : "1";
        }
        System.out.println(name + " WRONG OPERATOR: " + operator);
        return "0";
    }

    @Override
    public void generate(Sentence sentence) {
        for(Token t: sentence.getTokens()){
            t.setAttributeValue(name, generate(t));
        }

    }
}
