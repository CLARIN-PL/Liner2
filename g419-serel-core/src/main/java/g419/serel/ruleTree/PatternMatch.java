package g419.serel.ruleTree;

import g419.corpus.structure.Token;
import g419.serel.parseRule.ParseRuleLexer;
import g419.serel.parseRule.ParseRuleParser;
import g419.serel.ruleTree.listeners.ParseRuleListenerImpl;
import g419.serel.ruleTree.listeners.ThrowingErrorListener;
import lombok.Data;
import lombok.ToString;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class PatternMatch {

  String rule;
  String relationType;
  NodeMatch rootNodeMatch;


  // method using Antlr4
  public static PatternMatch parseRule(final String rule) {

    final PatternMatch patternMatch = new PatternMatch();
    patternMatch.setRelationType(rule);

    final ParseRuleLexer lexer = new ParseRuleLexer(CharStreams.fromString(rule));
    final ParseRuleParser parser = new ParseRuleParser(new CommonTokenStream(lexer));

    final ParseRuleListenerImpl listener = new ParseRuleListenerImpl();
    parser.addParseListener(listener);
    final ThrowingErrorListener errorListener = ThrowingErrorListener.INSTANCE;
    parser.addErrorListener(errorListener);
    parser.start();

    patternMatch.setRelationType(listener.relationType);
    patternMatch.setRootNodeMatch(listener.rootNodeMatch);

    return patternMatch;
  }

  public NodeMatch getALeaf() {
    return this.getALeaf(this.rootNodeMatch);
  }


  public NodeMatch getALeaf(final NodeMatch nodeMatch) {
    if (nodeMatch == null) {
      return null;
    }
    if (nodeMatch.isLeaf()) {
      return nodeMatch;
    } else {
      return getALeaf(nodeMatch.getEdgeMatchList().get(0).getNodeMatch());
    }
  }

  public List<Integer> getSentenceBranchMatchingUpPatternBranchFromNode(final NodeMatch startNodeMatch, final List<Token> tokens, final int startTokenIndex) {

    final List<Integer> result = new ArrayList<>();

    int tokenIndex = startTokenIndex;
    Token token = tokens.get(tokenIndex);
    NodeMatch nodeMatch = startNodeMatch;

    boolean found = false;
    while (nodeMatch.matches(token)) {
      result.add(tokenIndex);
      if (nodeMatch.getParentEdgeMatch() != null) {
        if (!nodeMatch.getParentEdgeMatch().matches(token)) {
          break;
        }
        nodeMatch = nodeMatch.getParentEdgeMatch().getParentNodeMatch();
        tokenIndex = Integer.valueOf(token.getAttributeValue("head"));
        if (tokenIndex == 0) {
          break;  // no more tokens but pattern still needs more
        }
        token = tokens.get(tokenIndex - 1);  // IDs are numbered from 1 in CoNLLu

      } else { // there is no up-links in pattern any more
        found = true;
        break;
      }
    }

    if (found) {
      return result;
    }

    return new ArrayList<>();
  }



/*
    public boolean isRuleElementMatchingSerelPathElement(final int ruleElementIndex, String serelPathElement, final boolean starMode ) {

        //System.out.println("isREMSPE: ruleElementIndex = "+ruleElementIndex);

        serelPathElement = serelPathElement.trim();

        final String text = ruleElements.get(ruleElementIndex).trim();
        final String depRel  = ruleElementsDeprel.get(ruleElementIndex).trim();

        String speText = "";
        String speDepRel = "";

        if (serelPathElement.length() > 0) {
            if (serelPathElement.charAt(serelPathElement.length() - 1) == ')') {
                final int indexStart = serelPathElement.lastIndexOf("(");
                if (indexStart != -1) {
                    speDepRel = serelPathElement.substring(indexStart + 1, serelPathElement.length() - 1).trim();
                    speText = serelPathElement.substring(0, indexStart).trim();
                }
            }
        }

        if( (text!=null) && (!text.isEmpty()) && (!text.equals("*")) ) {
            if(!text.equals(speText)) {
                //System.out.println(" no match for texts: "+text+" vs "+speText);
                return false;
            }
        }

        //TODO : *

        if( (depRel!=null) && (!depRel.isEmpty())  ) {
            if(!depRel.equals(speDepRel)) {
                //System.out.println(" no match for depRels:"+depRel+" vs "+speDepRel);
                return false;
            }
        }

        return true;
    }
*/


}
