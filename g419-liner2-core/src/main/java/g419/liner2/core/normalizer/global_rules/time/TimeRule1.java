package g419.liner2.core.normalizer.global_rules.time;

import g419.liner2.core.normalizer.global_rules.AbstractRule;

public class TimeRule1 extends AbstractRule {
  @Override
  protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
    return lval.replace("xxxx-xx-xx", firstNotNull(first, creationDate)).replace("t", "T");
  }

  @Override
  public boolean matches(String lval, String base) {
    return lval.startsWith("xxxx-xx-xx");
  }
}
