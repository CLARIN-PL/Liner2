package g419.serel.ruleTree;

import g419.corpus.structure.Token;
import lombok.Data;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = "parentEdgeMatch")
public class NodeMatch {

  private int id;

  private boolean isMatchAnyText;
  private boolean isMatchLemma;
  private String text;

  private String xPos;

  private String namedEntity;
  private String role;


  private EdgeMatch parentEdgeMatch;
  private List<EdgeMatch> edgeMatchList = new ArrayList<>();

  public NodeMatch() {
    System.out.println("!!!!! creating NodeMatch");
    /*
    try {
      throw new RuntimeException();
    } catch (final RuntimeException rt) {
      rt.printStackTrace();
    }
    */
  }

  public void dumpString() {
    System.out.println("NodeMatch: text = " + text + " xPos=" + xPos + " namedEntity=" + namedEntity + " role=" + role);
    for (final EdgeMatch em : getEdgeMatchList()) {
      em.dumpString();
    }
  }

  public boolean isLeaf() {
    return edgeMatchList.size() == 0;
  }


  public boolean matches(final Token token) {

    //text
    if (!isMatchAnyText) {
      if (!isMatchLemma) {
        if (!text.equals(token.getAttributeValue("orth"))) {  //form //word
          return false;
        }
      } else {
        if (!text.equals(token.getAttributeValue("lemma"))) {
          return false;
        }
      }
    }

    //xPos
    if ((xPos != null) && (!xPos.isEmpty())) {
      if (!token.getAttributeValue("xpos").startsWith(xPos)) {
        return false;
      }
    }

    //namedEntity
    if ((namedEntity != null) && (!namedEntity.isEmpty())) {

      final List<String> boiList = token.getBois();

      boolean found = false;
      for (final String boi : boiList) {
        if (boi.equals("B-" + namedEntity)) {  // "B-"  -> start token of entity
          found = true;
        }
      }

      if (!found) {
        return false;
      }
    }


    return true;
  }


}
