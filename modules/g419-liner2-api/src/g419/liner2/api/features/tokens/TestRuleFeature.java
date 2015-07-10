package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michal on 5/14/15.
 */
public class TestRuleFeature extends TokenInSentenceFeature {

    Pattern featurePattern = Pattern.compile("(\\((.+?)\\)\\[([-0-9]+)\\](!=|=)\"(.*?)\";)");
    public ArrayList<String> sourceFeats, values, operators;
    public ArrayList<Integer> offsets;
    String label;
    public String rule;

    public TestRuleFeature(String name, String rule) {
        super(name);
        this.rule = rule;
        sourceFeats = new ArrayList<>();
        offsets = new ArrayList<>();
        operators = new ArrayList<>();
        values = new ArrayList<>();
//        System.out.println("NAME: " + name);
//        System.out.println("RULE: " + rule);
        Matcher m = featurePattern.matcher(rule);
        while(m.find()){
            sourceFeats.add(m.group(2));
            offsets.add(Integer.parseInt(m.group(3)));
            operators.add(m.group(4));
            values.add(m.group(5));
        }
        label = String.valueOf(rule.substring(rule.lastIndexOf(";") + 1));
//        if(!label.equals("B") && !label.equals("I") && !label.equals("O")){
//            System.out.println(label + " | " + rule);
//        }

//        for(int i=0; i< sourceFeats.size(); i++){
//            System.out.println(sourceFeats.get(i) + " | " + offsets.get(i) + " | " + operators.get(i) + " | " + values.get(i));
//        }
    }

    @Override
    public void generate(Sentence sentence) {
        ArrayList<Token> tokens = sentence.getTokens();
        tokenLoop:
        for (int tokenIdx=0; tokenIdx<sentence.getTokenNumber(); tokenIdx++){
//            System.out.println(tokens.get(tokenIdx).getOrth() + " >>>>>>>>>>>>>");
            for(int feat=0; feat<sourceFeats.size(); feat++){
                if(!featureMatches(feat, tokenIdx + offsets.get(feat), tokens)){
                    tokens.get(tokenIdx).setAttributeValue(name, "0");
//                    System.out.println("REJECTED: " + rule);
                    continue tokenLoop;
                }

            }
//            System.out.println("MATCH: " + rule);
            tokens.get(tokenIdx).setAttributeValue(name, label);
        }
    }

    private boolean featureMatches(int feat, int tokenIdx, ArrayList<Token> tokens){
        String operator = operators.get(feat);
        String tokenValue;
        if(tokenIdx < 0 || tokenIdx >= tokens.size()){
            tokenValue = "out_of_range";
            return false;
//            System.out.println("INVALID INDEX");
        }
        else{
            tokenValue =  tokens.get(tokenIdx).getAttributeValue(sourceFeats.get(feat));
        }
        if(tokenValue == null){
            tokenValue = "null";
        }
//        System.out.println(tokenValue + operators.get(feat) + values.get(feat));
        if(operator.equals("=")){
            return tokenValue.equals(values.get(feat));
        }
        else if(operator.equals("!=")){
            return !tokenValue.equals(values.get(feat));
        }
        else{
            System.out.println(name + " WRONG OPERATOR: " + operator);
            return false;
        }
    }
}
