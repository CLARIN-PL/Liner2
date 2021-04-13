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

  public String getPathAsString(final boolean withIndexes) {
    final List<Token> tokens = relationDesc.getSentence().getTokens();
    return getPathAsStringCommon(tokens, true);
  }

  private String getPathAsStringCommon(final List<Token> tokens) {
    return getPathAsStringCommon(tokens, true);
  }

  private String getPathAsStringCommon(final List<Token> tokens, final boolean withAssociations) {
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
        .limit(Math.max(0, linksChainUpToParents1.size() - (skipLastElement ? 1 : 0)))
        .map(msl -> "(" + msl.getRelationType() + ") > " + sentenceLinkUp2String(msl, false))
        .collect(Collectors.joining(" ")));

    if (skipLastElement) {
      final SentenceLink lastSl = linksChainUpToParents1.get(Math.max(0, linksChainUpToParents1.size() - 1));
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

  private String sentenceLinkUp2String(final SentenceLink sl, final boolean withIndexes) {

    return
        getSentence()
            .getTokens()
            .get(sl.getTargetIndex()).getAttributeValue(0)
            +
            getCaseClauseForTokenIndex(sl.getTargetIndex());
  }

  private String getCaseClauseForTokenIndex(final int tokenIndex) {
    final List<SentenceLink> peerLinks = this.parseTree.getLinksByTargetIndex(tokenIndex);
    for (final SentenceLink peerLink : peerLinks) {
      if (peerLink.getRelationType().equals("case")) {
        final Token t = getSentence().getTokens().get(peerLink.getSourceIndex());
        System.out.println(t.getAttributeValue(2));
        if (t.getAttributeValue(2).startsWith("prep")) {
          return " # " + t.getDisambTag().getBase();
        }
      }
    }
    return "";
  }


}
