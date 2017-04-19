package g419.liner2.api.normalizer.lval;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by kotu on 12.01.17.
 */
public class LValRule {
    public String desc;
    public ArrayList<String> keys;
    public ArrayList<String> groups;
    public String match;
    public Map<String, String> lemmaTags;
    public Map<String, String> map;
    public ArrayList<String> limit;
    public Map<String, String> value;
    public Pattern pattern;
    public Pattern patternOrth;

    public boolean checkLemmaTags(Annotation annotation){
        if (lemmaTags != null) {
            for (Map.Entry<String, String> item : lemmaTags.entrySet()) {
                String ruleLemma = item.getKey();
                String ruleTag = item.getValue();

                for (Token token : annotation.getTokenTokens()) {
                    for (Tag tag : token.getTags()) {
                        if (tag.getDisamb() && tag.getBase().toLowerCase().equals(ruleLemma) && !tag.getCtag().contains(ruleTag)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;

    }

}
