package g419.serel.structure;

import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.core.tools.parser.ParseTree;
import g419.liner2.core.tools.parser.SentenceLink;
import lombok.Data;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents serel expression, which consists of:
 *
 * @author molek
 */
@Data
public class SerelExpression {


  private RelationDesc relationDesc;
  private List<SentenceLink> linksChainUpToParents1;
  private List<SentenceLink> linksChainUpToParents2;
  private ParseTree parseTree;
  private int index1;
  private int index2;


  public SerelExpression() {
  }

  public SerelExpression(final RelationDesc relDesc,
                         final List<SentenceLink> p1,
                         final List<SentenceLink> p2,
                         final ParseTree pt,
                         final int i1,
                         final int i2
  ) {
    this.relationDesc = relDesc;
    this.linksChainUpToParents1 = p1;
    this.linksChainUpToParents2 = p2;
    this.parseTree = pt;
    this.index1 = i1;
    this.index2 = i2;
  }


  public Sentence getSentence() {
    return this.relationDesc.getSentence();
  }

  public String getPathAsString() {
    return getPathAsString(false);
  }

/*
  public String getDetailedPathAsString(final boolean withIndexes) {
    final List<Token> tokens = relationDesc.getSentence().getTokens();
    if ((parents1 == null) || (parents2 == null)) {
      return " ";
    }

    final StringBuilder s = new StringBuilder();
    //s.append(relationDesc.getType()).append(": ");

    if (withIndexes) {
      s.append("|" + parents1.get(0).getSourceIndex() + "|");
    }


    s.append("[" + relationDesc.getFromType() + "]");
    s.append("(" + parents1.get(0).getRelationType() + ")");
    if (parents1.size() > 1) {
      s.append(" > ");
      s.append(parents1.stream()
          .skip(1)
          .map(msl -> sentenceLink2DetailedString(msl, withIndexes))
          .collect(Collectors.joining(" > ")));
    }

    final List<SentenceLink> tmpList = parents2.stream().skip(1).collect(Collectors.toList());

    Collections.reverse(tmpList);
    if (tmpList.size() > 1) {
      s.append(" < ");
      s.append(tmpList.stream()
          .skip(1)
          .map(msl -> sentenceLink2DetailedString(msl, withIndexes))
          .collect(Collectors.joining(" < ")));
    }
    s.append(" < ");
    if (withIndexes) {
      s.append("|" + parents2.get(0).getSourceIndex() + "|");
    }
    s.append("[" + relationDesc.getToType() + "]");
    s.append("(" + parents2.get(0).getRelationType() + ")");

    return s.toString();

  }
 */

  public String getPathAsString(final boolean withIndexes) {
    final List<Token> tokens = relationDesc.getSentence().getTokens();
    return getPathAsStringCommon(tokens, true);
  }

  private String getPathAsStringCommon(final List<Token> tokens) {
    return getPathAsStringCommon(tokens, true);
  }

  private String getPathAsStringCommon(final List<Token> tokens, final boolean withAssociations) {

//    if ((linksChainUpToParents1 == null) || (linksChainUpToParents2 == null)) {
//      return " ";
//    }
    final StringBuilder s = new StringBuilder();
    s.append(getSentence().getDocument().getName() + ";\t\t");
    s.append(relationDesc.getType()).append(";\t\t");
    s.append(relationDesc.getFromType()).append(";\t\t");
    s.append(relationDesc.getToType()).append(";\t\t");
    s.append(relationDesc.getType()).append("::");

    // "left" side
    // left anchor
    s.append(" * " + getCaseClauseForTokenIndex(index1) + " / " + relationDesc.getFromType() + ":e1");

    boolean skipLastElement = false;
    if (linksChainUpToParents2.size() == 0) {
      skipLastElement = true;
    }

    s.append(linksChainUpToParents1.stream()
        .limit(linksChainUpToParents1.size() - (skipLastElement ? 1 : 0))
        .map(msl -> "(" + msl.getRelationType() + ") > " + sentenceLinkUp2String(msl, false))
        .collect(Collectors.joining(" ")));

    if (skipLastElement) {
      final SentenceLink lastSl = linksChainUpToParents1.get(linksChainUpToParents1.size() - 1);
      s.append("(" + lastSl.getRelationType() + ") > ");
    }

    if (linksChainUpToParents2.size() > 0) {
      final List<SentenceLink> reversedList = linksChainUpToParents2.stream().collect(Collectors.toList());
      Collections.reverse(reversedList);

      final SentenceLink firstSl = reversedList.get(0);
      s.append(" < (" + firstSl.getRelationType() + ")");

      s.append(reversedList.stream()
          .skip(1)
          .map(msl -> sentenceLinkUp2String(msl, false) + " < (" + msl.getRelationType() + ")")
          .collect(Collectors.joining(" ")));
    }

    s.append(" *" + getCaseClauseForTokenIndex(index2) + " / " + relationDesc.getToType() + ":e2");

    final String sentExt = getSentence().toString();

    s.append(";\t\t\t" + sentExt.substring(0, Math.min(300, sentExt.length())));
    return s.toString();
  }

/*
  private String getPathAsStringCommonOld(final List<Token> tokens, final boolean withAssociations) {
    if ((linksChainUpToParents1 == null) || (linksChainUpToParents2 == null)) {
      return " ";
    }

    final StringBuilder s = new StringBuilder();
    s.append(getSentence().getDocument().getName() + ";\t\t");
    s.append(relationDesc.getType()).append(";\t\t");
    s.append(relationDesc.getFromType()).append(";\t\t");


    s.append(relationDesc.getToType()).append(";\t\t");
    s.append(relationDesc.getType()).append("::");


    // "left" side

    // left anchor
//    s.append(" * " + caseTail2String(relationDesc.getFromTokenIndex()) + " / " + relationDesc.getFromType() + ":e1");
//    s.append(" *  / " + relationDesc.getFromType() + ":e1");
    s.append(" * " + getCaseClause(linksChainUpToParents1.get(0)) + " / " + relationDesc.getFromType() + ":e1");

    if (linksChainUpToParents1.size() > 1) {
      if (withAssociations) {
        s.append(" (" + linksChainUpToParents1.get(0).getRelationType() + ")");
        s.append(" > ");
      }

      // process all left but the last one
      if (linksChainUpToParents1.size() > 2) {
        s.append(linksChainUpToParents1.stream()
            .skip(1)
            .limit(linksChainUpToParents1.size() - 1 - 1)  // additional -1 becasue of skip(1)
            .map(msl -> sentenceLink2String(msl, false) + " ( " + msl.getRelationType() + " ) ")
            .collect(Collectors.joining(" > ")));
        s.append(" > ");
      }

      //s.append(sentenceLink2String(parents1.get(parents1.size() - 1), false)); //LCD
    }

    if (linksChainUpToParents1.size() == 1) {
      s.append(" < ");
    }

    if (linksChainUpToParents2.size() == 1) {
      s.append(" *" + getCaseClause(linksChainUpToParents2.get(0)) + " / " + relationDesc.getToType() + ":e2");
    } else if (linksChainUpToParents2.size() > 1) {

      final List<SentenceLink> noAnchorList = linksChainUpToParents2.stream().skip(1).collect(Collectors.toList()); // skipping anchor
      Collections.reverse(noAnchorList);
      if (noAnchorList.size() == 1) { // only LCD

        if (linksChainUpToParents1.size() > 1) {
          s.append(sentenceLink2String(noAnchorList.get(0), false)); // append LCD
          s.append(" < ");
        }
        if (withAssociations) {
          s.append(" (" + linksChainUpToParents2.get(0).getRelationType() + ")");
        }
        s.append(" *" + getCaseClause(noAnchorList.get(0)) + "/ " + relationDesc.getToType() + ":e2");
      } else if (noAnchorList.size() > 1) { // skipping LCD
        if (linksChainUpToParents1.size() > 1) {
          s.append(sentenceLink2String(noAnchorList.get(0), false)); // append LCD
          s.append(" < ");
        }
        s.append(noAnchorList.stream()
            .skip(1)
            .map(msl -> " ( " + msl.getRelationType() + " ) " + sentenceLink2String(msl, false))
            .collect(Collectors.joining(" < ")));
        // handling anchor
        s.append(" < ");
        if (withAssociations) {
          s.append(" (" + linksChainUpToParents2.get(0).getRelationType() + ")");
        }
        s.append(" *" + getCaseClause(linksChainUpToParents2.get(0)) + " / " + relationDesc.getToType() + ":e2");
      }
    }


    //final String sentExt = relationDesc.getSentenceDecorated();

    final String sentExt = getSentence().toString();

    s.append(";\t\t\t" + sentExt.substring(0, Math.min(300, sentExt.length())));
    return s.toString();
  }
*/

  private String tokenIndex2String(final int tokenIndex, final boolean withIndexes) {
    final List<Token> tokens = getSentence().getTokens();
    final Token t = tokens.get(tokenIndex);

    return t.getAttributeValue(0)
        + getCaseClauseForTokenIndex(tokenIndex);

  }

  private String sentenceLinkUp2String(final SentenceLink sl, final boolean withIndexes) {

    final List<Token> tokens = getSentence().getTokens();
//    System.out.println("TOKENS = "+tokens);
//    System.out.println("SL SOURCE INDEX = "+sl.getSourceIndex());
    final Token t = tokens.get(sl.getSourceIndex());
    //String tail = t.getDisambTag().getBase();
    //String tail = t.getAttributeValue("lemma");
    //String tail = t.getOrth();
    String tail = t.toString();
    tail = tail + "(" + sl.getRelationType() + ")";


    final String standardResult = t.getAttributeValue(0);
    final String caseExtension = getCaseClause(sl);


    return
/*
            (withIndexes? "["+sl.getSourceIndex()+"]": "" ) +
            getCaseClause(sl) + tail;

*/
        //getCaseClause(sl) +

        getSentence()
            .getTokens()
            .get(sl.getTargetIndex()).getAttributeValue(0)
            +
            getCaseClauseForTokenIndex(sl.getTargetIndex());

    //.getDisambTag()
    //.getBase();
  }


  private String sentenceLink2String(final SentenceLink sl, final boolean withIndexes) {

    final List<Token> tokens = getSentence().getTokens();
//    System.out.println("TOKENS = "+tokens);
//    System.out.println("SL SOURCE INDEX = "+sl.getSourceIndex());
    final Token t = tokens.get(sl.getSourceIndex());
    //String tail = t.getDisambTag().getBase();
    //String tail = t.getAttributeValue("lemma");
    //String tail = t.getOrth();
    String tail = t.toString();
    tail = tail + "(" + sl.getRelationType() + ")";


    final String standardResult = t.getAttributeValue(0);
    final String caseExtension = getCaseClause(sl);


    return
/*
            (withIndexes? "["+sl.getSourceIndex()+"]": "" ) +
            getCaseClause(sl) + tail;

*/
        //getCaseClause(sl) +

        getSentence()
            .getTokens()
            .get(sl.getSourceIndex()).getAttributeValue(0)
            +
            getCaseClause(sl);

    //.getDisambTag()
    //.getBase();
  }

  private boolean isWithCaseClause(final SentenceLink sl) {
    final List<SentenceLink> peerLinks = this.parseTree.getLinksByTargetIndex(sl.getSourceIndex());
    for (final SentenceLink peerLink : peerLinks) {
      if (peerLink.getRelationType().equals("case")) {
        return true;
      }
    }
    return false;
  }

  private String getCaseClause(final SentenceLink sl) {
    return getCaseClauseForTokenIndex(sl.getSourceIndex());
  }

  private String getCaseClauseForTokenIndex(final int tokenIndex) {
    final List<SentenceLink> peerLinks = this.parseTree.getLinksByTargetIndex(tokenIndex);
    for (final SentenceLink peerLink : peerLinks) {
      if (peerLink.getRelationType().equals("case")) {
        return " # " + getSentence().getTokens().get(peerLink.getSourceIndex()).getDisambTag().getBase();
      }
    }
    return "";
  }


  private String sentenceLink2DetailedString(final SentenceLink sl, final boolean withIndexes) {

    final List<Token> tokens = getSentence().getTokens();
    //System.out.println("TOKENS = "+tokens);
    //System.out.println("SL SOURCE INDEX = "+sl.getSourceIndex());
    final Token t = tokens.get(sl.getSourceIndex());
    //String tail = t.getDisambTag().getBase();
    String tail = t.getAttributeValue("lemma");
    tail = tail + "(" + sl.getRelationType() + ")";

    return
        (withIndexes ? "|" + sl.getSourceIndex() + "|" : "") +
            /*getCaseClause(sl) +*/ tail;
  }

  /*
  private String getCaseClauseForTokenIndex(final int tokenIndex) {

    final List<SentenceLink> subLinks = parseTree.getLinksByTargetIndex(tokenIndex);

    for (final SentenceLink sl : subLinks) {
      final Token t = this.getSentence().getTokens().get(sl.getSourceIndex());
      if (t.getAttributeValue("depRel").equals("case")) {
        System.out.println("Found case depRel");
        return "# " + t.getOrth();
      }
    }
    return "";
  }

   */

  public void enhanceWithCaseMode(final String caseMode) {
    if ((caseMode != null) && (!caseMode.equals("none"))) {


    }
  }


}
