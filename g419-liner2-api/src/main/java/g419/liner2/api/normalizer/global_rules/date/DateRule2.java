package g419.liner2.api.normalizer.global_rules.date;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class DateRule2 extends AbstractRule{
    @Override
    public boolean matches(String lval, String base) {
        return lval.equals("xxxx-xx-xx");
    }

    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        return firstNotNull(previous, creationDate);
    }
}
