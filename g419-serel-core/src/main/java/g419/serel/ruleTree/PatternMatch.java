package g419.serel.ruleTree;

import com.google.common.collect.Lists;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.serel.parseRule.ParseRuleLexer;
import g419.serel.parseRule.ParseRuleParser;
import g419.serel.ruleTree.listeners.ParseRuleListenerImpl;
import g419.serel.ruleTree.listeners.ThrowingErrorListener;
import g419.serel.structure.patternMatch.PatternMatchExtraInfo;
import g419.serel.structure.patternMatch.PatternMatchSingleResult;
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

  public List<Integer>
  getSentenceBranchMatchingUpPatternBranchFromNode(
      final NodeMatch startNodeMatch,
      //final List<Token> tokens,
      final Sentence sentence,
      final int startTokenIndex,
      final PatternMatchExtraInfo extraInfo
  ) {
    return getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, sentence, startTokenIndex, Collections.emptySet(), extraInfo);
  }

  public List<Integer>
  getSentenceBranchMatchingUpPatternBranchFromNode(
      final NodeMatch startNodeMatch,
      //final List<Token> tokens,
      final Sentence sentence,
      final int startTokenIndex,
      final Set<Integer> excludedNodesIds,
      final PatternMatchExtraInfo extraInfo) {
    final List<Integer> result = new ArrayList<>();

    int tokenIndex = startTokenIndex;
    Token token = sentence.getTokens().get(tokenIndex);
    NodeMatch nodeMatch = startNodeMatch;

    boolean found = false;
    while (nodeMatch.matches(token, extraInfo)) {
      result.add(tokenIndex);

//   multi-node star:
//      if (nodeMatch.isMatchAnyTotal() && (nodeMatch.getParentEdgeMatch() != null) && nodeMatch.getParentEdgeMatch().isMatchAnyDepRel()) {
//        matchTotalStarToPattern(tokens, tokenIndex, nodeMatch);
//      } else {

      if (nodeMatch.getParentEdgeMatch() != null) {
        if (!nodeMatch.getParentEdgeMatch().matches(token)) {
          break;
        }
        nodeMatch = nodeMatch.getParentEdgeMatch().getParentNodeMatch();
        final int parentTokenId = token.getParentTokenId();
        if (parentTokenId == 0) {
          break;  // no more tokens but pattern still needs more
        }
        tokenIndex = parentTokenId - 1; // IDs are numbered from 1 in CoNLLu, indexes in list  - from 0
        token = sentence.getTokens().get(tokenIndex);

      } else { // there is no up-links in pattern any more
        found = true;
        break;
      }

//      }

    }

    if (found) {
      return result;
    }

    return new ArrayList<>();
  }

/*
  List<Integer> matchTotalStarToPattern(final List<Token> tokens, final int tokenIndex, final NodeMatch nodeMatch) {
    final List<Integer> result = new ArrayList<>();
    result.add(tokenIndex);     // since its total star we know that any starting token matches

    // if there is no more parts of patterns we just return this token. If there are more parts of patterns ...
    if (nodeMatch.getParentEdgeMatch() != null) {

      // what is our boundary ?
      final NodeMatch nextNodeMatch = nodeMatch.getParentEdgeMatch().getNodeMatch();
      assert (!nextNodeMatch.isMatchAnyTotalWithDepRel()); // we assume no consequtive 'total' stars, it means no patterns like: text > * > * > text

      boolean checkingEdge = false;
      if (nextNodeMatch.isMatchAnyTotal()) {
        assert (!nextNodeMatch.getParentEdgeMatch().isMatchAnyDepRel());
        // we seek depRel
        checkingEdge = true;
      } else {
        // we seek token with text, xPos or namedEntity
        checkingEdge = false;
      }

      Token token = tokens.get(tokenIndex);
      // ok - now we know what to search for ...
      boolean nextMatchPresent = false;
      do {
        final int parentTokenId = token.getParentTokenId();

        if (parentTokenId == 0) {
          //  there is no more sentence to match against pattern
          // we give back what we had gathered so far
          break;
        }

        final Token parentToken = tokens.get(parentTokenId - 1);
        if (!checkingEdge) {
          nextMatchPresent = nextNodeMatch.matches(parentToken);
        } else {
          nextMatchPresent = nextNodeMatch.getParentEdgeMatch().matches(parentToken);
        }
        result.add(parentTokenId - 1);

        token = parentToken;
      } while (nextMatchPresent);

    }

    return result;
  }
*/

  public List<PatternMatchSingleResult> getSentenceTreesMatchingRule(final Sentence sentence) {


    final List<PatternMatchSingleResult> result = new ArrayList<>();


    // take first branch from pattern tree
    // just get its last leaf
    final Optional<NodeMatch> optFirstLeafNodeMatch = this.getALeaf();
    final NodeMatch firstLeafNodeMatch = optFirstLeafNodeMatch.get();

    // is there another branch in pattern to search match for ?
    final Set<Integer> excludedIds = new HashSet<>();
    excludedIds.add(firstLeafNodeMatch.getId());
    final Optional<NodeMatch> optSecondLeafNodeMatch = this.getALeaf(excludedIds);


    //final List<Token> tokens = sentence.getTokens();
    // try to match a subtree in sentence
    for (int i = 0; i < sentence.getTokens().size(); i++) {
      final PatternMatchExtraInfo pmei = new PatternMatchExtraInfo();
      pmei.setSentence(sentence);

      final Token token = sentence.getTokens().get(i);
      if (firstLeafNodeMatch.matches(token, pmei)) {
        // can we find match for the whole first pattern-branch ?
        final List<Integer> foundFirstBranch = this.getSentenceBranchMatchingUpPatternBranchFromNode(firstLeafNodeMatch, sentence, i, pmei);
        if (foundFirstBranch.size() == 0) {
          continue;
        }

        // if we found and there is no second branch in pattern this is our whole result
        if (!optSecondLeafNodeMatch.isPresent()) {
          result.add(new PatternMatchSingleResult(foundFirstBranch, pmei));
          continue;
        }

        // we have second branch of pattern

        final NodeMatch secondLeafNodeMatch = optSecondLeafNodeMatch.get();

        for (int j = 0; j < sentence.getTokens().size(); j++) {
          if (j == i) { // from this point we already have the first branch found
            continue;
          }
          final Token secToken = sentence.getTokens().get(j);
          if (secondLeafNodeMatch.matches(secToken, pmei)) {
            final List<Integer> foundSecondBranch = this.getSentenceBranchMatchingUpPatternBranchFromNode(secondLeafNodeMatch, sentence, j, new HashSet(foundFirstBranch), pmei);
            //
            if (foundSecondBranch.size() != 0) {
              // do both branches have really the same root, or just the same text (orth,namedEntity) in different roots ?
              final int firstBranchRootId = foundFirstBranch.get(foundFirstBranch.size() - 1);
              final int secondBranchRootId = foundSecondBranch.get(foundSecondBranch.size() - 1);

              if (firstBranchRootId == secondBranchRootId) {
                foundSecondBranch.remove(foundSecondBranch.size() - 1);
                final List<Integer> reversedSecondBraachWithoutRoot = Lists.reverse(foundSecondBranch);
                foundFirstBranch.addAll(reversedSecondBraachWithoutRoot);
                result.add(new PatternMatchSingleResult(foundFirstBranch, pmei));
                //System.out.println("OPR:" + foundFirstBranch);
              }
            }
            continue;
          }
        }

      }
    }
    return result;
  }


}
