package g419.liner2.core.normalizer.global_rules.date;

import g419.liner2.core.normalizer.global_rules.AbstractRule;

public class DateRule7 extends AbstractRule {
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        String used = firstNotNull(previous, creationDate);
        return lval.replaceFirst("xxxx-xx", year(used)+"-"+month(used));
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.startsWith("xxxx-xx");
    }
}
