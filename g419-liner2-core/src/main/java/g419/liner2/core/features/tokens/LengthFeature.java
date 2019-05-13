package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class LengthFeature extends TokenFeature {

  public LengthFeature(String name) {
    super(name);
  }

  public String generate(Token t, TokenAttributeIndex index) {
    return "" + t.getAttributeValue(index.getIndex("orth")).length();
  }


}
