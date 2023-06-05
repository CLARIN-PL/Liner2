package g419.liner2.core.normalizer.lval;

import java.util.regex.Pattern;

public class LValRuleCompiled extends LValRule {
  public Pattern pattern = null;
  public Pattern patternOrth = null;

  public LValRuleCompiled(LValRule rule, Pattern pattern){
    this.desc = rule.desc;
    this.keys = rule.keys;
    this.groups = rule.groups;
    this.match = rule.match;
    this.lemmaTags = rule.lemmaTags;
    this.map = rule.map;
    this.limit = rule.limit;
    this.value = rule.value;
    this.pattern = pattern;
  }

}
