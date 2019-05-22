package g419.liner2.core.normalizer.global_rules.date;

import g419.liner2.core.normalizer.global_rules.AbstractRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DateRule5 extends AbstractRule {
  @Override
  public boolean matches(String lval, String base) {
    return lval.startsWith("-") || lval.startsWith("+");
  }

  @Override
  protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
    String sign = "" + lval.charAt(0);
    List<String> parts = new ArrayList<>(Arrays.asList(lval.substring(1).split("-")));
    if (parts.size() == 3) {
      if (lval.contains("WE")) {
        return year(creationDate) + "-Wxx-WE";
      }
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
      );
    } else if (parts.size() == 2) {
      if (lval.contains("W")) {
        return year(creationDate) + "-Wxx";
      }
      try {
        String r = toString(
            addYears(
                addMonths(
                    fromString(creationDate),
                    sign + parts.get(1)
                ),
                sign + parts.get(0)
            )
        );
        return year(r) + "-" + month(r);
      } catch (RuntimeException ignored) {
        return year(
            toString(
                addYears(
                    fromString(creationDate),
                    sign + parts.get(0)
                )
            )
        ) + "-" + parts.get(1);
      }
    }
    try {
      return year(
          toString(
              addYears(
                  fromString(creationDate),
                  sign + parts.get(0)
              )
          )
      );
    } catch (RuntimeException ignored) {
    }
    return null;
  }
}
