package g419.liner2.api.normalizer.global_rules.date;

import g419.liner2.api.normalizer.global_rules.AbstractRule;
import org.joda.time.LocalDate;

public class DateRule10 extends AbstractRule{
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        LocalDate result = closestWeekday(
                fromString(creationDate),
                sign(lval.substring(0,1))+lval.substring(2, 3)
        );
        if (result.equals(fromString(creationDate)))
            result = addDays(result, sign(lval.substring(0, 1))+"7");
        return toString(
                result
        );
//        throw new RuntimeException("TUTAJ lval: "+lval+"; base: "+base+"; prev: "+previous+";");
    }

    String sign(String arrow){
        if (arrow.equals("<"))
            return "-";
        else if (arrow.equals(">"))
            return "";
        throw new RuntimeException("That shouldnt happen");
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.matches("[<>].*");
    }
}
