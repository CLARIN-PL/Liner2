package g419.liner2.api.normalizer.global_rules.time;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class TimeRule2 extends AbstractRule{
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        return creationDate+"txx:xx";
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.contains("T+");
    }
}
