package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class HasLowerFeature extends TokenFeature {

  public HasLowerFeature(String name) {
    super(name);
  }

  public String generate(Token token, TokenAttributeIndex index) {
    for (char c : token.getAttributeValue(index.getIndex("orth")).toCharArray()) {
      if (Character.isLowerCase(c)) {
        return "1";
      }
    }
    return "0";
  }
}