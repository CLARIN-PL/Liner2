package g419.serel.ruleTree;

import g419.corpus.structure.Token;
import g419.serel.structure.patternMatch.PatternMatchExtraInfo;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
  }

  public void dumpString() {
    log.debug("NodeMatch: text = " + text + " xPos=" + xPos + " namedEntity=" + namedEntity + " role=" + role);
    for (final EdgeMatch em : getEdgeMatchList()) {
      em.dumpString();
    }
  }

  public boolean isLeaf() {
    return edgeMatchList.size() == 0;
  }

  public boolean isMatchAnyTotal() {
    return isMatchAnyText
      && ((xPos == null) || (xPos.isEmpty()))
      && ((namedEntity == null) || (namedEntity.isEmpty()));
  }

  public boolean hasAnnotation() {
    return (namedEntity != null) && (!namedEntity.trim().isEmpty());
  }

  public boolean isMatchAnyTotalWithDepRel() {
    return isMatchAnyTotal()
      && ((parentEdgeMatch == null) || parentEdgeMatch.isMatchAnyDepRel());
  }


  public boolean matches(final Token token) {
    return this.matches(token, null);
  }

  public boolean matches(final Token token, final PatternMatchExtraInfo extraInfo) {

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

          if ((role != null) && (!role.isEmpty())) {
            extraInfo.putRole(role, namedEntity, token);
          }

        }
      }

      if (!found) {
        return false;
      }
    }


    return true;
  }


}
