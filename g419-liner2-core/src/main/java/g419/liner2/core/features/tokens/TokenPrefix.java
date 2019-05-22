package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class TokenPrefix extends TokenFeature {

  private int index = 0;
  private int prefixLength = 0;

  /**
   * @param index        — feature index for which the prefix will be generated,
   * @param prefixLength — prefix length in characters.
   */
  public TokenPrefix(String name, int index, int prefixLength) {
    super(name);
    this.index = index;
    this.prefixLength = prefixLength;
  }

  @Override
  public String generate(Token token, TokenAttributeIndex index) {
    String value = token.getAttributeValue(this.index);
    int n = Math.min(this.prefixLength, value.length() - 1);
    return value.substring(0, n);
  }


}
