package g419.liner2.api.normalizer.global_rules.time;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class TimeRule4 extends AbstractRule{
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        try {
            String day = lval.substring(0, 10).split("-")[2];
            return toString(closestWeekday(fromString(creationDate), day))+lval.substring(10);
        } catch (Throwable t){
            return null;
        }
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.startsWith("xxxx-Wxx-");
    }
}
