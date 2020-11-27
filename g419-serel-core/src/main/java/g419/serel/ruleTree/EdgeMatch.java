package g419.serel.ruleTree;


import g419.corpus.structure.Token;
import lombok.Data;

@Data
public class EdgeMatch {

  private String side;

  private boolean matchAnyDepRel;
  private String depRel;


  private NodeMatch parentNodeMatch;
  private NodeMatch nodeMatch;

  public EdgeMatch() {
  }

  public void dumpString() {
    System.out.println("EdgeMatch side=" + side + " depRel=" + depRel);
    nodeMatch.dumpString();
  }

  /**
   * Always tries to match only the link from token to parent
   *
   * @param token
   * @return
   */

  public boolean matches(final Token token) {

    if (!matchAnyDepRel) {
      if (!depRel.equals(token.getAttributeValue("deprel"))) {
        return false;
      }
    }

    return true;
  }


}
