package g419.liner2.api.normalizer.global_rules.time;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class TimeRule5 extends AbstractRule{
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        return lval.replaceFirst("xxxx", year(creationDate));
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.startsWith("xxxx");
    }
}
