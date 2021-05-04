package g419.serel.ruleTree;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.serel.structure.patternMatch.PatternMatchExtraInfo;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Data
@ToString(exclude = "parentEdgeMatch")
public class NodeMatch {

  private int id;

  private boolean isMatchAnyText;
  private boolean isMatchLemma;
  //private String text;
  private Set<String> texts = new HashSet<>();

  private String uPos;
  private String xPos;


  private String namedEntity;
  private String role;

  private List<String> functionNames = new ArrayList<>();

  private EdgeMatch parentEdgeMatch;
  private List<EdgeMatch> edgeMatchList = new ArrayList<>();

  private String caseTail;

  public NodeMatch() {
  }

  public void dumpString() {
    log.debug("NodeMatch: texts = " + texts + " xPos=" + xPos + " namedEntity=" + namedEntity + " role=" + role);
    for (final EdgeMatch em : getEdgeMatchList()) {
      em.dumpString();
    }
  }

  public boolean isLeaf() {
    return edgeMatchList.size() == 0;
  }

  public boolean isForNamedEntity() { return ((namedEntity != null) && (!namedEntity.isEmpty())); }

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
    return this.matches(token, extraInfo, null);
  }

  public boolean matches(final Token token, final PatternMatchExtraInfo extraInfo, final Sentence sentence) {

    //text
    if (!isMatchAnyText) {
      if (!isMatchLemma) {
        if (!texts.contains(token.getAttributeValue("orth"))) {  //form //word
          return false;
        }
      } else {
        if (!texts.contains(token.getAttributeValue("lemma"))) {
          return false;
        }
      }
    }

    if ((uPos != null) && (!uPos.isEmpty())) {
      if (!token.getAttributeValue("upos").equals(uPos)) {
        return false;
      }
    }


    if ((xPos != null) && (!xPos.isEmpty())) {
      if (!token.getAttributeValue("xpos").equals(xPos)) {
        return false;
      }
    }

//
//    if (!token.getAttributeValue("xpos").startsWith(xPos)) {
//      return false;
//    }

    //namedEntity
    if (this.isForNamedEntity()) {

      final List<String> boiList = token.getBois();

      boolean found = false;
      for (final String boi : boiList) {

        if (
            (boi.equals("B-" + namedEntity))  // "B-"  -> start token of entity
                ||
                (boi.equals("I-" + namedEntity))  // "I-"  -> inside token of entity
        ) {

          // z ta wersją są problemy gdy "chwyci" nie tą nazwę co trzeba ale też pasującą ...
//        if (
//            (boi.startsWith("B-" + namedEntity))  // "B-"  -> start token of entity
//                ||
//                (boi.startsWith("I-" + namedEntity))  // "I-"  -> inside token of entity
//        ) {

// TOREVERT
//          // w aktualnej wersji tu jest za wczesnie na to sprawdzanie bo łączenie wszystkieog odbywa się dopiero
//          // "po" przejściu pasujących ścieżek
//          // oraz jeśli to nie jest BOI które już wcześniej znaleźliśmy i do czegoś przypięliśmy !!!
//          final String possibleAlreadyFoundTag = extraInfo.getTagNEFromToken(token);
//          if (possibleAlreadyFoundTag != null) {
//            continue; // to jest NE i nawet pasuje ale już wcześniej jakiś NodeMatch go zaanektował
//          }

          found = true;
          //TODO byc moze to przeniesc wyzej i uniezaleznic wpisytwanie tych info od tego czy jest rola czy nie ma
          if ((role != null) && (!role.isEmpty())) {
            //extraInfo.putRole(role, namedEntity, token);
            extraInfo.putRole(role, boi.substring(2), token); // zapamiętujemy rzeczywisty tag NE a nie ten z wzorca - po to by potem idki dobrze dobierać
          }

        }
      }

      if (!found) {
        return false;
      }
    }


    // # case
    if ((this.getCaseTail() != null) && (!this.getCaseTail().equals(""))) {

      final List<Token> childTokens = sentence.getChildrenTokensFromTokenId(token.getNumberId());
      boolean foundMatchingCase = false;
      for (final Token t : childTokens) {
        if ("case".equals(t.getAttributeValue("deprel"))) {
          if (caseTail.equals(t.getAttributeValue("lemma"))) {
            foundMatchingCase = true;
            break;
          }
        }
      }
      if (!foundMatchingCase) {
        return false;
      }
    }


    return true;
  }


}
