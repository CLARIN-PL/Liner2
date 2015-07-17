package g419.liner2.api.normalizer.global_rules.date;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class DateRule11 extends AbstractRule{
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        return "20"+base;
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.equals("VAGUE") && base.startsWith("0");
    }
}
