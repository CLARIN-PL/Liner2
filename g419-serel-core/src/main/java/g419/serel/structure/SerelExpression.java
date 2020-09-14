package g419.serel.structure;

import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.core.tools.parser.ParseTree;
import g419.liner2.core.tools.parser.SentenceLink;
import lombok.Data;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents serel expression, which consists of:
 *
 * @author molek
 */
@Data
public class SerelExpression {


  private Relation relation;
  private List<SentenceLink> parents1;
  private List<SentenceLink> parents2;
  private ParseTree maltSentence;


  public SerelExpression() {
  }

  public SerelExpression(final Relation rel,
                         List<SentenceLink> p1,
                         List<SentenceLink> p2,
                         ParseTree ms) {
    this.relation = rel;
    this.parents1 = p1;
    this.parents2 = p2;
    this.maltSentence = ms;
  }


  public Sentence getSentence() {
    return this.relation.getAnnotationFrom().getSentence();
  }

  public String getPathAsString() {
    if ((parents1 == null) || (parents2 == null) ) {
      return " ";
    }

    List<Token> tokens = relation.getAnnotationFrom().getSentence().getTokens();
    StringBuilder s = new StringBuilder();
    s.append(relation.getType()).append(": ");

    s.append(relation.getAnnotationFrom().getType()); //.append(" -> ");
    if(parents1.size()>1) {
      s.append(" -> ");
      s.append(parents1.stream()
          .skip(1)
          .map(msl -> tokens.get(msl.getSourceIndex()).getDisambTag().getBase())
          .collect(Collectors.joining(" -> ")));
    }

    List<SentenceLink> tmpList = parents2.stream().skip(1).collect(Collectors.toList());
    Collections.reverse(tmpList);
    if(tmpList.size()>1) {
      s.append(" <- ");
      s.append(parents2.stream()
          .skip(1)
          .map(msl -> tokens.get(msl.getSourceIndex()).getDisambTag().getBase())
          .collect(Collectors.joining(" <- ")));
    }
    s.append(" <- ").append(relation.getAnnotationTo().getType());

    return s.toString();
  }



}
