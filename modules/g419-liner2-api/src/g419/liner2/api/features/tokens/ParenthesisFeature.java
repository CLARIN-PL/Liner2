package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by michal on 8/25/14.
 */
public class ParenthesisFeature extends TokenInSentenceFeature {

    static HashSet<String> openingCharacters = new HashSet<String>(Arrays.asList("(", "[", "{"));
    static HashSet<String> closingCharacters = new HashSet<String>(Arrays.asList(")", "]", "}"));

    public ParenthesisFeature(String name) {
        super(name);
    }


    @Override
    public void generate(Sentence sentence) {
        int thisFeatureIdx = sentence.getAttributeIndex().addAttribute(this.getName());
        int inParanthesis = 0;
        for(Token t: sentence.getTokens()){
            if(openingCharacters.contains(t.getOrth())){
                t.setAttributeValue(thisFeatureIdx, "B");
                inParanthesis++;
            }
            else if(closingCharacters.contains(t.getOrth())){
                t.setAttributeValue(thisFeatureIdx, "E");
                inParanthesis--;
            }
            else if(inParanthesis > 0){
                t.setAttributeValue(thisFeatureIdx, "I");
            }
            else{
                t.setAttributeValue(thisFeatureIdx, "O");
            }
        }
    }
}
