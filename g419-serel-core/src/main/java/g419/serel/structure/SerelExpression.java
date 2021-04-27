package g419.serel.structure;


import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.core.tools.parser.ParseTree;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents serel expression, which consists of:
 *
 * @author molek
 */
@Data
public class SerelExpression {


  private RelationDesc relationDesc;
  private List<Token> tokensChainUp1;
  private List<Token> tokensChainUp2;

  private ParseTree parseTree;
  private int index1;
  private int index2;


  public SerelExpression() {
  }

  public SerelExpression(final RelationDesc relDesc,
                         final List<Token> p1,
                         final List<Token> p2,
                         final int i1,
                         final int i2
  ) {
    this.relationDesc = relDesc;
    this.tokensChainUp1 = p1;
    this.tokensChainUp2 = p2;
    this.index1 = i1;
    this.index2 = i2;
  }

  public Sentence getSentence() {
    return this.relationDesc.getSentence();
  }


  public String getPathAsString() {
    final StringBuilder s = new StringBuilder();
    s.append(getSentence().getDocument().getName() + ";\t\t");
    s.append(relationDesc.getType()).append(";\t\t");
    s.append(relationDesc.getFromType()).append(";\t\t");
    s.append(relationDesc.getToType()).append(";\t\t");

    s.append(getJustPattern());

    final String sentExt = getSentence().toString();

    s.append(";\t\t\t" + sentExt.substring(0, Math.min(300, sentExt.length())));
    return s.toString();
  }


  public Pair<List<Token>, Token> getJustPatternInTokens() {

    if ((tokensChainUp1.size() < 2) && (tokensChainUp2.size() < 2)) {
      System.out.println("ERROR !!! Nieprawidłowe ścieżki łączące relacje ");
    }

    final Token changeDepRelDirection;

    // left
    final ArrayList<Token> left = new ArrayList<>();
    left.add(tokensChainUp1.get(0));

    if (tokensChainUp1.size() > 1) {
      for (int i = 1; i <= tokensChainUp1.size() - 1 - 1; i++) {
        final Token t = tokensChainUp1.get(i);
        left.add(t);
      }
    }

    //right
    final ArrayList<Token> right = new ArrayList<>();
    if (tokensChainUp2.size() > 1) {
      for (int i = tokensChainUp2.size() - 1 - 1; i >= 1; i--) {
        final Token t = tokensChainUp2.get(i);
        right.add(t);
      }
    }
    right.add(tokensChainUp2.get(0));
//  System.out.println("RIGHT =" + right);

    final ArrayList<Token> total = new ArrayList<>();
    if ((tokensChainUp1.size() == 1) || (tokensChainUp2.size() == 1)) {
      total.addAll(left);
      total.addAll(right);
      changeDepRelDirection = left.get(left.size() - 1);
    } else {
      final Token tCenter = tokensChainUp1.get(tokensChainUp1.size() - 1);
//    System.out.println("SCENTER = " + tCenter);
      total.addAll(left);
      total.add(tCenter);
      changeDepRelDirection = tCenter;
      total.addAll(right);
    }

    return Pair.of(total, changeDepRelDirection);
  }


  public StringBuilder getJustPattern() {
    if ((tokensChainUp1.size() < 2) && (tokensChainUp2.size() < 2)) {
      System.out.println("ERROR !!! Nieprawidłowe ścieżki łączące relacje ");
    }

//    System.out.println("\n\n");

    // "left" side
    // left anchor
    final StringBuilder sLeft = new StringBuilder();
    sLeft.append(relationDesc.getType()).append("::");
    sLeft.append(" *" + getCaseClauseForTokenIndex(tokensChainUp1.get(0).getNumberId() - 1) + " / " + relationDesc.getFromType() + ":e1");

    if (tokensChainUp1.size() > 1) {
      final Token t0 = tokensChainUp1.get(0);
      sLeft.append(getDepRelClauseForToken(t0) + " > ");

      for (int i = 1; i <= tokensChainUp1.size() - 1 - 1; i++) {
        final Token t = tokensChainUp1.get(i);
        sLeft.append(token2String(t, false) + getDepRelClauseForToken(t) + " > ");
      }
    }
//  System.out.println("SLEFT =" + sLeft);

    //right Side
    final StringBuffer sRight = new StringBuffer();
    if (tokensChainUp2.size() > 1) {
      for (int i = tokensChainUp2.size() - 1 - 1; i >= 1; i--) {
        final Token t = tokensChainUp2.get(i);
        sRight.append(" < " + getDepRelClauseForToken(t) + token2String(t, false));
      }
      final Token t = tokensChainUp2.get(0);
      sRight.append(" < " + getDepRelClauseForToken(t));
    }
    sRight.append(" *" + getCaseClauseForTokenIndex(tokensChainUp2.get(0).getNumberId() - 1) + " / " + relationDesc.getToType() + ":e2");
//  System.out.println("SRIGHT =" + sRight);

    final StringBuilder sTotal = new StringBuilder();
    if ((tokensChainUp1.size() == 1) || (tokensChainUp2.size() == 1)) {
      sTotal.append(sLeft);
      sTotal.append(sRight);
    } else {
      final Token tCenter = tokensChainUp1.get(tokensChainUp1.size() - 1);
      final String sCenter = token2String(tCenter, false);
//    System.out.println("SCENTER = " + sCenter);
      sTotal.append(sLeft);
      sTotal.append(sCenter);
      sTotal.append(sRight);
    }

//  System.out.println("STOTAL = " + sTotal);
    return sTotal;
  }

  private String token2String(final Token t, final boolean withIndexes) {


    if (t.getBoisNonEmpty().size() > 0) {
      // to jest jakiś NE
      return
          " * "
              +
              getCaseClauseForTokenIndex(t.getNumberId() - 1)  // id -> index
              + " / " +
              t.getBoisNonEmpty().get(0).substring(2);
    }
    

    // to nie jest żaden NE
    return
        t.getAttributeValue(1)
            +
            getCaseClauseForTokenIndex(t.getNumberId() - 1);  // id -> index

  }

  private String getCaseClauseForTokenIndex(final int tokenIndex) {

    //SWITCH_2

    final List<Token> children = getSentence().getChildrenTokensFromTokenIndex(tokenIndex);

    for (final Token token : children) {
//      System.out.println(" CASE check token" + token);
      if (token.getAttributeValue("deprel").equals("case")) {
//        System.out.println(token.getAttributeValue(2));
        if (token.getAttributeValue(4).startsWith("prep")) {
          return " # " + token.getAttributeValue(2);
        }
      }
    }

    return "";
  }

  private String getDepRelClauseForToken(final Token t) {
    //SWITCH_3

    final String depRel = t.getAttributeValue("deprel");
    return " (" + depRel + ") ";

    //return "";
  }

}
