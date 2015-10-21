package g419.liner2.api.normalizer.global_rules.date;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class DateRule9 extends AbstractRule {
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        if(lval.length() == 4 && lval.charAt(2)=='0')
                return "20"+lval.substring(2, Math.min(4, lval.length()));
            else
                return "19"+lval.substring(2, Math.min(4, lval.length()));
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.startsWith("xx");
    }
}
