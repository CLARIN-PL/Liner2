package g419.liner2.api.normalizer.global_rules.date;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

public class DateRule6 extends AbstractRule {
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        String[] parts = split(lval);
        if (parts[2].equals("WE")){
            return year(creationDate) + "-Wxx-WE";
        }
        try {
            return toString(
                    closestWeekday(
                            fromString(creationDate),
                            parts[2]
                    )
            );
        } catch (NumberFormatException nfe){
            return null;
        }
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.startsWith("xxxx-Wxx-");
    }
}
