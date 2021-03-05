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
  private List<SentenceLink> parents1;
  private List<SentenceLink> parents2;
  private ParseTree parseTree;


  public SerelExpression() {
  }

  public SerelExpression(final RelationDesc relDesc,
                         final List<SentenceLink> p1,
                         final List<SentenceLink> p2,
                         final ParseTree pt) {
    this.relationDesc = relDesc;
    this.parents1 = p1;
    this.parents2 = p2;
    this.parseTree = pt;
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
    if ((parents1 == null) || (parents2 == null)) {
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
    s.append(" * / " + relationDesc.getFromType() + ":e1");

    if (parents1.size() > 1) {
      if (withAssociations) {
        s.append(" (" + parents1.get(0).getRelationType() + ")");
        s.append(" > ");
      }

      // process all left but the last one
      if (parents1.size() > 2) {
        s.append(parents1.stream()
            .skip(1)
            .limit(parents1.size() - 1 - 1)  // additional -1 becasue of skip(1)
            .map(msl -> sentenceLink2String(msl, false) + " ( " + msl.getRelationType() + " ) ")
            .collect(Collectors.joining(" > ")));
        s.append(" > ");
      }

      //s.append(sentenceLink2String(parents1.get(parents1.size() - 1), false)); //LCD
    }

    if (parents1.size() == 1) {
      s.append(" < ");
    }

    if (parents2.size() == 1) {
      s.append(" * / " + relationDesc.getToType() + ":e2");
    } else if (parents2.size() > 1) {

      final List<SentenceLink> noAnchorList = parents2.stream().skip(1).collect(Collectors.toList()); // skipping anchor
      Collections.reverse(noAnchorList);
      if (noAnchorList.size() == 1) { // only LCD

        if (parents1.size() > 1) {
          s.append(sentenceLink2String(noAnchorList.get(0), false)); // append LCD
          s.append(" < ");
        }
        if (withAssociations) {
          s.append(" (" + parents2.get(0).getRelationType() + ")");
        }
        s.append(" * / " + relationDesc.getToType() + ":e2");
      } else if (noAnchorList.size() > 1) { // skipping LCD
        if (parents1.size() > 1) {
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
          s.append(" (" + parents2.get(0).getRelationType() + ")");
        }
        s.append(" * / " + relationDesc.getToType() + ":e2");
      }
    }


    //final String sentExt = relationDesc.getSentenceDecorated();

    final String sentExt = getSentence().toString();

    s.append(";\t\t\t" + sentExt.substring(0, Math.min(300, sentExt.length())));
    return s.toString();
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

    return
/*
            (withIndexes? "["+sl.getSourceIndex()+"]": "" ) +
            getCaseClause(sl) + tail;

*/
        //getCaseClause(sl) +

        getSentence()
            .getTokens()
            .get(sl.getSourceIndex()).getAttributeValue(0);
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
    final List<SentenceLink> peerLinks = this.parseTree.getLinksByTargetIndex(sl.getSourceIndex());
    for (final SentenceLink peerLink : peerLinks) {
      if (peerLink.getRelationType().equals("case")) {
        return "(" + getSentence().getTokens().get(peerLink.getSourceIndex()).getDisambTag().getBase() + ") ";
        //return "(" + getSentence().getTokens().get(peerLink.getSourceIndex()).getAttributeValue("lemma") + ") ";
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


}
