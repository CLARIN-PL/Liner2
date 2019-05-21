package g419.liner2.core.normalizer.global_rules.date;

import g419.liner2.core.normalizer.global_rules.AbstractRule;

public class DateRule8 extends AbstractRule {
  @Override
  protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
    return year(firstNotNull(previous, creationDate)) + lval.substring(4);
  }

  @Override
  public boolean matches(String lval, String base) {
    return lval.startsWith("xxxx");
  }
}
