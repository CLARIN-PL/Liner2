package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class PrefixFeature extends TokenFeature {

  private int n;

  public PrefixFeature(String name) {
    super(name);
    this.n = Integer.parseInt(name.substring(name.length() - 1));
  }

  public String generate(Token t, TokenAttributeIndex index) {
    String orth = t.getAttributeValue(index.getIndex("orth"));
    if (orth.length() >= n) {
      return orth.substring(0, n);
    } else {
      StringBuilder sb = new StringBuilder(orth);
      for (int i = 0; i < (n - orth.length()); i++) {
        sb.append("_");
      }
      return sb.toString();
    }
  }

}
