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


  public String getDetailedPathAsString(final boolean withIndexes) {
    final List<Token> tokens = relationDesc.getSentence().getTokens();
    if ((parents1 == null) || (parents2 == null) ) {
      return " ";
    }

    final StringBuilder s = new StringBuilder();
    //s.append(relationDesc.getType()).append(": ");

    if(withIndexes) {
      s.append("|" + parents1.get(0).getSourceIndex() + "|");
    }




    s.append("["+ relationDesc.getFromType()+"]");
    s.append("(" +parents1.get(0).getRelationType()+ ")");
    if(parents1.size()>1) {
      s.append(" > ");
      s.append(parents1.stream()
              .skip(1)
              .map(msl -> sentenceLink2DetailedString(msl,withIndexes))
              .collect(Collectors.joining(" > ")));
    }

    final List<SentenceLink> tmpList = parents2.stream().skip(1).collect(Collectors.toList());

    Collections.reverse(tmpList);
    if(tmpList.size()>1) {
      s.append(" < ");
      s.append(tmpList.stream()
              .skip(1)
              .map(msl ->sentenceLink2DetailedString(msl,withIndexes))
              .collect(Collectors.joining(" < ")));
    }
    s.append(" < ");
    if(withIndexes) {
      s.append("|" + parents2.get(0).getSourceIndex() + "|");
    }
    s.append("["+relationDesc.getToType()+"]");
    s.append("(" +parents2.get(0).getRelationType()+ ")");

    return s.toString();

  }

  public String getPathAsString(final boolean withIndexes) {
    final List<Token> tokens = relationDesc.getSentence().getTokens();
    return getPathAsStringCommon(tokens, withIndexes);
  }


  private String getPathAsStringCommon(final List<Token> tokens, final boolean withIndexes) {
    if ((parents1 == null) || (parents2 == null) ) {
      return " ";
    }


    final StringBuilder s = new StringBuilder();
    s.append(relationDesc.getType()).append(": ");

    if(withIndexes) {
      s.append("[" + parents1.get(0).getSourceIndex() + "]");
    }
    s.append(relationDesc.getFromType());
    if(parents1.size()>1) {
      s.append(" > ");
      s.append(parents1.stream()
          .skip(1)
          .map(msl -> sentenceLink2String(msl,withIndexes))
          .collect(Collectors.joining(" > ")));
    }

    final List<SentenceLink> tmpList = parents2.stream().skip(1).collect(Collectors.toList());
    Collections.reverse(tmpList);
    if(tmpList.size()>1) {
      s.append(" < ");
      s.append(tmpList.stream()
          .skip(1)
          .map(msl ->sentenceLink2String(msl,withIndexes))
          .collect(Collectors.joining(" < ")));
    }
    s.append(" < ");
    if(withIndexes) {
        s.append("[" + parents2.get(0).getSourceIndex() + "]");
    }
    s.append(relationDesc.getToType());

    return s.toString();
  }

  private String sentenceLink2String(final SentenceLink sl, final boolean withIndexes) {

    final List<Token> tokens = getSentence().getTokens();
//    System.out.println("TOKENS = "+tokens);
//    System.out.println("SL SOURCE INDEX = "+sl.getSourceIndex());
    final Token t = tokens.get(sl.getSourceIndex());
    //String tail = t.getDisambTag().getBase();
    String tail = t.getAttributeValue("lemma");
    tail = tail + "(" + sl.getRelationType() + ")";

    return
            (withIndexes? "["+sl.getSourceIndex()+"]": "" ) +
            getCaseClause(sl) + tail;

/*
            getSentence()
                    .getTokens()



                    .get(
                            sl
                                    .getSourceIndex())
                    .getDisambTag()
                    .getBase();
*/
  }

  private boolean isWithCaseClause(final SentenceLink sl) {
    final List<SentenceLink> peerLinks = this.parseTree.getLinksByTargetIndex(sl.getSourceIndex());
    for(final SentenceLink peerLink : peerLinks) {
      if(peerLink.getRelationType().equals("case")) {
        return true;
      }
    }
    return false;
  }

  private String getCaseClause(final SentenceLink sl ) {
    final List<SentenceLink> peerLinks = this.parseTree.getLinksByTargetIndex(sl.getSourceIndex());
    for(final SentenceLink peerLink : peerLinks) {
      if(peerLink.getRelationType().equals("case")) {
        //  return "("+getSentence().getTokens().get(peerLink.getSourceIndex()).getDisambTag().getBase()+") ";
        return "("+getSentence().getTokens().get(peerLink.getSourceIndex()).getAttributeValue("lemma")+") ";
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
            (withIndexes? "|"+sl.getSourceIndex()+"|": "" ) +
                    /*getCaseClause(sl) +*/ tail;
  }



}
