package g419.serel.structure.patternMatch;

import g419.corpus.structure.Token;

public class NamedEntityWithToken {
  String namedEntity;
  Token token;

  @Override
  public String toString() {
    return "[" + namedEntity + " : " + token.getAttributeValue("orth") + " ]";
  }
}
