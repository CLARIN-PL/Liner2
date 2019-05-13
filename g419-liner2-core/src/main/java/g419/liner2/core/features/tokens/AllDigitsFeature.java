package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class AllDigitsFeature extends TokenFeature {

  private Pattern ALL_DIGITS = Pattern.compile("^\\p{N}+$");

  public AllDigitsFeature(String name) {
    super(name);
  }

  public String generate(Token t, TokenAttributeIndex index) {
    String orth = t.getAttributeValue(index.getIndex("orth"));
    if (ALL_DIGITS.matcher(orth).find()) {
      return "1";
    } else {
      return "0";
    }

  }


}
