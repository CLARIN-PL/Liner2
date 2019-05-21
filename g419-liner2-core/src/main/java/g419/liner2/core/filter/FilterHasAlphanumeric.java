package g419.liner2.core.filter;

import g419.corpus.structure.Annotation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilterHasAlphanumeric extends Filter {

  private static final String hasAlpanumeric = "(\\p{Lu}|\\p{Ll}|[0-9])";
  private static Pattern pattern = null;

  public FilterHasAlphanumeric() {
    appliesTo.add("PERSON_FIRST_NAM");
    appliesTo.add("PERSON_LAST_NAM");
    appliesTo.add("CITY_NAM");
    appliesTo.add("COUNTRY_NAM");
    appliesTo.add("ROAD_NAM");

    pattern = Pattern.compile(hasAlpanumeric);
  }

  @Override
  public String getDescription() {
    return "Has alphanumeric char: " + hasAlpanumeric;
  }

  @Override
  public Annotation pass(final Annotation chunk, final CharSequence charSeq) {
    final Matcher m = pattern.matcher(charSeq);
    if (m.find()) {
      return chunk;
    } else {
      return null;
    }
  }

}
