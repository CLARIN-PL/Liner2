package g419.corpus.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michal on 1/8/15.
 */
public class WrappedToken extends Token {
  ArrayList<Token> oldTokens;
  Sentence oldSentence;

  public WrappedToken(final String orth, final Tag firstTag, final TokenAttributeIndex attrIdx) {
    super(orth, firstTag, attrIdx);
    oldTokens = new ArrayList<>();
  }

  public WrappedToken(final String orth, final Tag firstTag, final TokenAttributeIndex attrIdx, final List<Token> oldTokens, final Sentence oldSentence) {
    super(orth, firstTag, attrIdx);
    this.oldTokens = (ArrayList<Token>) oldTokens;
    this.oldSentence = oldSentence;
  }

  public void addToken(final Token t) {
    oldTokens.add(t);
  }

  public void setOldSentence(final Sentence s) {
    oldSentence = s;
  }

  public String getFullOrth() {
    String str = "";
    for (final Token token : oldTokens) {
      if (!noSpaceAfter) {
        str += " ";
      }
      if (token instanceof WrappedToken) {
        str += ((WrappedToken) token).getFullOrth();
      } else {
        str += token.getOrth();
      }
    }
    return str.trim();
  }
}
