package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class AllUpperFeature extends TokenFeature {

  private Pattern ALL_UPPER = Pattern.compile("^\\p{Lu}+$");

  public AllUpperFeature(String name) {
    super(name);
  }

  public String generate(Token t, TokenAttributeIndex index) {
    String orth = t.getAttributeValue(index.getIndex("orth"));
    if (ALL_UPPER.matcher(orth).find()) {
      return "1";
    } else {
      return "0";
    }

  }


}
