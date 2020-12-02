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
import java.util.*;

@Data
@ToString
public class PatternMatch {

  String rule;
  String relationType;
  NodeMatch rootNodeMatch;
  List<NodeMatch> nodeMatchList;


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
    patternMatch.setNodeMatchList(listener.nodeMatchList);

    return patternMatch;
  }

  public Optional<NodeMatch> getALeaf() {
    return this.getALeaf(this.rootNodeMatch);
  }

  public Optional<NodeMatch> getALeaf(final NodeMatch nodeMatch) {
    return this.getALeaf(nodeMatch, Collections.emptySet());
  }

  public Optional<NodeMatch> getALeaf(final Set<Integer> excludedNodesIds) {
    return this.getALeaf(this.rootNodeMatch, excludedNodesIds);
  }

  public Optional<NodeMatch> getALeaf(final NodeMatch nodeMatch, final Set<Integer> excludedNodesIds) {
    return nodeMatchList.stream().filter(node -> node.isLeaf() && !excludedNodesIds.contains(node.getId())).findAny();
  }

  public List<Integer> getSentenceBranchMatchingUpPatternBranchFromNode(final NodeMatch startNodeMatch, final List<Token> tokens, final int startTokenIndex) {
    return getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex, Collections.emptySet());
  }

  public List<Integer> getSentenceBranchMatchingUpPatternBranchFromNode(final NodeMatch startNodeMatch, final List<Token> tokens, final int startTokenIndex, final Set<Integer> excludedNodesIds) {

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
        tokenIndex--; // IDs are numbered from 1 in CoNLLu, indexes in list  - from 0
        token = tokens.get(tokenIndex);

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


  public List<Set<Integer>> getSentenceTreesMatchingRule(final List<Token> tokens) {

    final List<Set<Integer>> result = new ArrayList<>();
    //final List<Token> tokens = sentence.getTokens();


    // take first branch from pattern tree
    // just get its last leaf
    final Optional<NodeMatch> optFirstLeafNodeMatch = this.getALeaf();
    final NodeMatch firstLeafNodeMatch = optFirstLeafNodeMatch.get();

    // is there another branch in pattern to search match for ?
    final Set<Integer> excludedIds = new HashSet<>();
    excludedIds.add(firstLeafNodeMatch.getId());
    final Optional<NodeMatch> optSecondLeafNodeMatch = this.getALeaf(excludedIds);


    // try to match a subtree in sentence
    for (int i = 0; i < tokens.size(); i++) {
      final Token token = tokens.get(i);
      if (firstLeafNodeMatch.matches(token)) {
        // can we find match for the whole first pattern-branch ?
        final List<Integer> foundFirstBranch = this.getSentenceBranchMatchingUpPatternBranchFromNode(firstLeafNodeMatch, tokens, i);
        if (foundFirstBranch.size() == 0) {
          continue;
        }

        // if we found and  there is no second branch in pattern this is our whole result
        if (!optSecondLeafNodeMatch.isPresent()) {
          result.add(new HashSet<>(foundFirstBranch));
          continue;
        }

        // we have second branch of pattern

        final NodeMatch secondLeafNodeMatch = optSecondLeafNodeMatch.get();

        for (int j = 0; j < tokens.size(); j++) {
          if (j == i) { // from this point we already have the first branch found
            continue;
          }
          final Token secToken = tokens.get(j);
          if (secondLeafNodeMatch.matches(secToken)) {
            final List<Integer> foundSecondBranch = this.getSentenceBranchMatchingUpPatternBranchFromNode(secondLeafNodeMatch, tokens, j, new HashSet(foundFirstBranch));
            //
            if (foundSecondBranch.size() != 0) {
              // do both branches have really the same root, or just the same text in different roots ?
              final int firstBranchRootId = foundFirstBranch.get(foundFirstBranch.size() - 1);
              final int secondBranchRootId = foundSecondBranch.get(foundSecondBranch.size() - 1);

              if (firstBranchRootId == secondBranchRootId) {
                final Set<Integer> onePossibleResult = new HashSet<>();
                onePossibleResult.addAll(foundFirstBranch);
                onePossibleResult.addAll(foundSecondBranch);
                result.add(onePossibleResult);
                System.out.println("OPR:" + onePossibleResult);
              }
            }
            continue;
          }
        }


      }
    }
    return result;
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
