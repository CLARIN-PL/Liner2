package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class StartsWithUpperFeature extends TokenFeature {

  public StartsWithUpperFeature(String name) {
    super(name);
  }

  public String generate(Token token, TokenAttributeIndex index) {
    if (Character.isUpperCase(token.getAttributeValue(index.getIndex("orth")).charAt(0))) {
      return "1";
    } else {
      return "0";
    }
  }
}
	

