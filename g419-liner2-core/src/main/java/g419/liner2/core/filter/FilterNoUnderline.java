package g419.liner2.core.filter;

import g419.corpus.structure.Annotation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilterNoUnderline extends Filter {

  private static final String hasSymbol = "([_])";
  private static Pattern pattern = null;

  public FilterNoUnderline() {
    appliesTo.add("PERSON_FIRST_NAM");
    appliesTo.add("PERSON_LAST_NAM");
    appliesTo.add("CITY_NAM");
    appliesTo.add("COUNTRY_NAM");
    appliesTo.add("ROAD_NAM");

    pattern = Pattern.compile(hasSymbol);
  }

  @Override
  public String getDescription() {
    return "Does not have any symbol: " + hasSymbol;
  }

  @Override
  public Annotation pass(final Annotation chunk, final CharSequence charSeq) {
    final Matcher m = pattern.matcher(charSeq);
    if (!m.find()) {
      return chunk;
    } else {
      return null;
    }
  }

}
