package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class StartsWithDigitFeature extends TokenFeature {

  private Pattern DIGITS = Pattern.compile("^\\p{N}");

  public StartsWithDigitFeature(String name) {
    super(name);
  }

  public String generate(Token token, TokenAttributeIndex index) {
    if (DIGITS.matcher(token.getAttributeValue(index.getIndex("orth"))).find()) {
      return "1";
    } else {
      return "0";
    }
  }
}