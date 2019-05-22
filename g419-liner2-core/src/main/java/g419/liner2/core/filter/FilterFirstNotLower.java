package g419.liner2.core.filter;

import g419.corpus.structure.Annotation;

public class FilterFirstNotLower extends Filter {

  private static final String firstLower = "([a-z]|ą|ż|ś|ę|ć|ń|ó|ł)";

  public FilterFirstNotLower() {
    appliesTo.add("PERSON_FIRST_NAM");
    appliesTo.add("PERSON_LAST_NAM");
    appliesTo.add("CITY_NAM");
    appliesTo.add("COUNTRY_NAM");
    appliesTo.add("ROAD_NAM");
  }

  @Override
  public String getDescription() {
    return "First not lower, does not match '" + firstLower + "'";
  }

  @Override
  public Annotation pass(final Annotation chunk, final CharSequence charSeq) {
    if (!charSeq.toString().matches(FilterFirstNotLower.firstLower)) {
      return chunk;
    } else {
      return null;
    }
  }

}
