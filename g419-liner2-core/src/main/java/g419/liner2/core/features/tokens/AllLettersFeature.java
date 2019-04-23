package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class AllLettersFeature extends TokenFeature {

  private Pattern ALL_LETTERS = Pattern.compile("^\\p{L}+$");

  public AllLettersFeature(String name) {
    super(name);
  }

  public String generate(Token t, TokenAttributeIndex index) {
    String orth = t.getAttributeValue(index.getIndex("orth"));
    if (ALL_LETTERS.matcher(orth).find()) {
      return "1";
    } else {
      return "0";
    }

  }


}
