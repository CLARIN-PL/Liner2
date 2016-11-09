package g419.liner2.api.normalizer.global_rules.time;

import g419.liner2.api.normalizer.global_rules.AbstractRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeRule3 extends AbstractRule{
    @Override
    protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
        String sign = "" + lval.charAt(0);
        List<String> parts = new ArrayList<>(Arrays.asList(lval.substring(1, 11).split("-")));
        if (parts.size() == 3) {
            return toString(
                    addYears(
                            addMonths(
                                    addDays(
                                            fromString(creationDate),
                                            sign + parts.get(2)
                                    ),
                                    sign + parts.get(1)
                            ),
                            sign + parts.get(0)
                    )
            ) + lval.substring(11);
        }
        return null;
    }

    @Override
    public boolean matches(String lval, String base) {
        return lval.matches("[-+].*");
    }
}
