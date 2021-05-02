package g419.serel.ruleTree;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.serel.parseRule.ParseRuleLexer;
import g419.serel.parseRule.ParseRuleParser;
import g419.serel.ruleTree.listeners.ParseRuleListenerImpl;
import g419.serel.ruleTree.listeners.ThrowingErrorListener;
import g419.serel.structure.patternMatch.PatternMatchExtraInfo;
import g419.serel.structure.patternMatch.PatternMatchSingleResult;
import g419.serel.tools.Permutations;
import lombok.Data;
import lombok.ToString;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import java.util.*;
import java.util.stream.Collectors;

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

  public List<NodeMatch> getAllLeaves() {
    return nodeMatchList.stream().filter(node -> node.isLeaf()).collect(Collectors.toList());
  }

  public List<String> getAllAnnotations() {
    return nodeMatchList.stream().filter(node -> node.hasAnnotation()).map(n -> n.getNamedEntity()).collect(Collectors.toList());
  }

  public List<PatternMatchSingleResult> getSentenceTreesMatchingGenericPattern(final Sentence sentence) {
    List<PatternMatchSingleResult> result = new ArrayList<>();

    for (final Token token : sentence.getTokens()) {
      final List<PatternMatchSingleResult> resultForOneToken = getSentenceTreesMatchingGenericPatternFromToken(sentence, token);
      result.addAll(resultForOneToken);
    }

    result.stream().forEach(pmsr -> pmsr.docName = sentence.getDocument().getName());
    result.stream().forEach(pmsr -> pmsr.sentenceNumber = sentence.sentenceNumber);

    result = makeDistinct(result);
    return result;
  }

  /*
  public void shiftIdsToIndexes(final List<PatternMatchSingleResult> input) {
    for (final PatternMatchSingleResult pmsr : input) {
      for (int i = 0; i < pmsr.idsList.size(); i++) {
        pmsr.idsList.set(i, pmsr.idsList.get(i) - 1);
      }
    }
  }
  */

  public List<PatternMatchSingleResult> makeDistinct(final List<PatternMatchSingleResult> input) {

    if (input.isEmpty()) {
      return input;
    }

    // preparing first, refernce PMEI to have somthing to compare to
    final List<PatternMatchSingleResult> result = new ArrayList<>();
    final PatternMatchSingleResult firstPmsr = input.get(0);
    //firstPmsr.idsList.sort(Comparator.naturalOrder());
    result.add(firstPmsr);

    outer:
    for (int i = 1; i < input.size(); i++) {
      final PatternMatchSingleResult currentPmsr = input.get(i);
      for (final PatternMatchSingleResult resultPmsr : result) {
        if (currentPmsr.isTheSameAs(resultPmsr)) {
          continue outer;
        }
      }

      result.add(currentPmsr);
    }

    return result;
  }


  public List<PatternMatchSingleResult> getSentenceTreesMatchingGenericPatternFromToken(final Sentence sentence, final Token token) {
    final List<PatternMatchSingleResult> result = getSentenceTreesMatchingGenericPatternFromTokenAndEdge(sentence, token, null, rootNodeMatch);
    return result;
  }

  public List<PatternMatchSingleResult>
  getSentenceTreesMatchingGenericPatternFromTokenAndEdge(final Sentence sentence,
                                                         final Token token,
                                                         final EdgeMatch edgeMatch,
                                                         final NodeMatch nodeMatch) {
    if (edgeMatch != null) { // used for matching rootNodeMatch
      if (!edgeMatch.matches(token)) {
        return Collections.emptyList(); // no match
      }
    }

    final PatternMatchExtraInfo pmei = new PatternMatchExtraInfo();
    pmei.setSentence(sentence);
    if (!nodeMatch.matches(token, pmei, sentence)) {
      return Collections.emptyList(); // no match
    }

    // here we now know there is a match ...

    final List<PatternMatchSingleResult> result = new LinkedList<>();

    final PatternMatchSingleResult oneResult;
    final String namedEntityLabel;
    //TOREVERT
    final LinkedHashSet<Integer> _idsList;
    if (nodeMatch.isForNamedEntity()) {
      _idsList = sentence.getBoiTokensIdsForTokenAndName(token, pmei.getTagNEFromToken(token));
    } else {
      _idsList = new LinkedHashSet<>();
      _idsList.add(token.getNumberId());
    }

//    System.out.println("_idsList = " + _idsList + " nmE=" + nodeMatch.isForNamedEntity());
//    System.out.println("Token =" + token);

    oneResult = new PatternMatchSingleResult(_idsList, pmei, this.getRelationType());
/*
    // TOREVERT ???
    try {

      if (nodeMatch.isForNamedEntity()) {
        namedEntityLabel = _idsList.get(0) + ":" + nodeMatch.getNamedEntity();
        oneResult.namedEntitySet.add(namedEntityLabel);
      }
    } catch (final Throwable th) {
      th.printStackTrace();
    }
*/

    if (nodeMatch.isLeaf()) {
      result.add(oneResult);
      // final match!It is leaf so we end this branch of recursion here
      return result;
    }
    // we here know at this level there is a match. But there are further levels since this token is not a leaf ...

    final List<Token> childrenTokens = sentence.getChildrenTokensFromToken(token);
    // if there is more branches in pattern then we have in actual sentence node we know there is no way to match it
    if (nodeMatch.getEdgeMatchList().size() > childrenTokens.size()) {
      return Collections.emptyList();
    }

    final List<List<Token>> childrenTokenSubCombinations = Permutations.getAllCombinations(childrenTokens, nodeMatch.getEdgeMatchList().size());
    for (final List<Token> childrenTokenCombination : childrenTokenSubCombinations) {
      // for each subcombinations generate all possible permutations and check each one
      final List<List<Token>> childrenTokenPermutations = Permutations.getAllPermutations(childrenTokenCombination);
      for (final List<Token> childrenTokenPermutation : childrenTokenPermutations) {

        final List<PatternMatchSingleResult> resultsForOnePermutation = getResultsForOnePermutation(sentence, childrenTokenPermutation, nodeMatch.getEdgeMatchList());
        result.addAll(resultsForOnePermutation);
      }
    }

    result.forEach(r -> r.concatenateWith(oneResult));
    return result;
  }

  private final List<PatternMatchSingleResult>
  getResultsForOnePermutation(final Sentence sentence,
                              final List<Token> childrenTokenPermutation,
                              final List<EdgeMatch> patternEdges) {
    List<PatternMatchSingleResult> onePermutationResults = new ArrayList<>();

    for (int i = 0; i < patternEdges.size(); i++) {

      final EdgeMatch nextEdgeMatch = patternEdges.get(i);
      final Token nextToken = childrenTokenPermutation.get(i);

      final List<PatternMatchSingleResult> resultsForOnePatternEdge =
          getSentenceTreesMatchingGenericPatternFromTokenAndEdge(sentence, nextToken, nextEdgeMatch, nextEdgeMatch.getNodeMatch());

      // each edge must match!
      if (resultsForOnePatternEdge.isEmpty()) {
        //check next permutation
        return Collections.emptyList();
      }

      if (onePermutationResults.isEmpty()) {
        onePermutationResults.addAll(resultsForOnePatternEdge);
      } else {
        final List<PatternMatchSingleResult> newOnePermutationResults = new ArrayList<>();

        for (final PatternMatchSingleResult oldPmsr : onePermutationResults) {
          for (final PatternMatchSingleResult pmsr : resultsForOnePatternEdge) {
            final PatternMatchSingleResult newPmsr = new PatternMatchSingleResult(oldPmsr);
            newPmsr.concatenateWith(pmsr);
            newOnePermutationResults.add(newPmsr);
          }
        }

        onePermutationResults = newOnePermutationResults;
      }
    }
    return onePermutationResults;
  }

}
