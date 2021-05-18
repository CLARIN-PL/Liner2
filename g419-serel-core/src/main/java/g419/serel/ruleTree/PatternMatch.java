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
      // może tu : validateAgainstNegativeSamples ?
      result.addAll(resultForOneToken);
    }

    result.stream().forEach(pmsr -> pmsr.docName = sentence.getDocument().getName());
    result.stream().forEach(pmsr -> pmsr.sentenceNumber = sentence.sentenceNumber);

    result = makeDistinct(result);

    result = validateAgainstNegativeSamples(result, sentence);


    return result;
  }


  private List<PatternMatchSingleResult> validateAgainstNegativeSamples(final List<PatternMatchSingleResult> sentenceResults, final Sentence sentence) {

    final List<PatternMatchSingleResult> result = new ArrayList<>();

    //List<PatternMatch> negativePatterns = mapPattern2NegativePatterns.get()

    outer:
    for (final PatternMatchSingleResult pmsr : sentenceResults) {

      if (pmsr.relationType.equals("alias")) {

        // jeśli pierwsza rola jest w klauzuli parataxis:insert to wynik odpada

        final Set<Integer> roleE1Ids = pmsr.patternMatchExtraInfo.getRoleE1Ids();
        final int headE1Id = sentence.findActualHeadIdForSetOfIds(roleE1Ids);

        Token headParent = sentence.getParentTokenFromTokenId(headE1Id);
        if (headParent != null) {
          if (headParent.getAttributeValue("deprel").equals("parataxis:insert")) {

            System.out.println("PMSR REJECTED v.1 " + pmsr + " SENT: " + sentence);
            continue;
          }


          // jesli nawiasy są i są naokoło pierwszej roli to wynik odpada
          {
            final List<Token> headsChildren = sentence.getChildrenTokensFromToken(headParent);

            boolean openP = false, closeP = false;
            for (final Token t : headsChildren) {
              if (t.getAttributeValue(1).equals("(")) {
                openP = true;
              }
              if (t.getAttributeValue(1).equals(")")) {
                closeP = true;
              }
            }
            if (openP && closeP) {
              System.out.println("PMSR REJECTED v.2 " + pmsr + " SENT: " + sentence);
              continue;
            }
          }
        }
        // jeśli druga rola jest podłączona za pomocą (conj) i na jej poziomie jest 2 lub więcej innych takich
        // elementów - ten sam xpos i deprel - to to jest lista  = wynik odpada

        final Set<Integer> roleE2Ids = pmsr.patternMatchExtraInfo.getRoleE2Ids();
        final int headE2Id = sentence.findActualHeadIdForSetOfIds(roleE2Ids);
        headParent = sentence.getParentTokenFromTokenId(headE2Id);
        if (headParent != null) {
          final List<Token> headsChildren = sentence.getChildrenTokensFromToken(headParent);

          int conjCounter = 0;
          for (final Token t : headsChildren) {
            if (t.getAttributeValue("deprel").equals("conj")) {
              conjCounter++;
              if (conjCounter >= 3) {
                System.out.println("PMSR REJECTED v.3 " + pmsr + " SENT: " + sentence);
                continue outer;
              }
            }
          }


        }
      }


      result.add(pmsr);
    }


    return result;
  }

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


  public List<PatternMatchSingleResult> getSentenceTreesMatchingGenericPatternFromToken(final Sentence sentence,
                                                                                        final Token token) {

    final List<PatternMatchSingleResult> output = new ArrayList<>();
//    final PatternMatchSingleResult pmsr = new PatternMatchSingleResult();
//    output.add(pmsr);

    final List<PatternMatchSingleResult> result = getSentenceTreesMatchingGenericPatternFromTokenAndEdge(output, sentence, token, null, rootNodeMatch);
    return result;
  }

  public List<PatternMatchSingleResult>
  getSentenceTreesMatchingGenericPatternFromTokenAndEdge(final List<PatternMatchSingleResult> output,
                                                         final Sentence sentence,
                                                         final Token token,
                                                         final EdgeMatch edgeMatch,
                                                         final NodeMatch nodeMatch) {
    if (edgeMatch != null) { // used for matching rootNodeMatch
      if (!edgeMatch.matches(token)) {
        return Collections.emptyList(); // no match
      }
    }


    final List<String> potentialFittingBOIs = new ArrayList<>();
    if (!nodeMatch.matches(token, potentialFittingBOIs, sentence)) {
      return Collections.emptyList(); // no match
    }
    // here we now know token match the pattern node ...
    // and in potentialFittingBois are all BOI that match restrictions of current NodeMatch


    // nawet tylko na tym pozionie lista bo rozpoznawanie NE może dać wiele wariantów
    final List<PatternMatchSingleResult> thisLevelResults = new ArrayList<>();


    if (nodeMatch.isForNamedEntity()) {
      //System.out.println("PotBOIS=" + potentialFittingBOIs);

// TOREVERT
//          // w aktualnej wersji tu jest za wczesnie na to sprawdzanie bo łączenie wszystkieog odbywa się dopiero
//          // "po" przejściu pasujących ścieżek
//          // oraz jeśli to nie jest BOI które już wcześniej znaleźliśmy i do czegoś przypięliśmy !!!
//          final String possibleAlreadyFoundTag = extraInfo.getTagNEFromToken(token);
//          if (possibleAlreadyFoundTag != null) {
//            continue; // to jest NE i nawet pasuje ale już wcześniej jakiś NodeMatch go zaanektował
//          }
      for (final String boi : potentialFittingBOIs) {
        final PatternMatchExtraInfo pmei = new PatternMatchExtraInfo();
        pmei.setSentence(sentence);

        //TODO byc moze to uniezaleznic wpisywanie tych info od tego czy jest rola czy nie ma
        if ((nodeMatch.getRole() != null) && (!nodeMatch.getRole().isEmpty())) {
          pmei.putRole(nodeMatch.getRole(), boi, token); // zapamiętujemy rzeczywisty tag NE a nie ten z wzorca - po to by potem idki dobrze dobierać
        }
        final LinkedHashSet<Integer> _idsList = sentence.getBoiTokensIdsForTokenAndName(token, pmei.getTagNEFromToken(token));
        //    System.out.println("_idsList = " + _idsList + " nmE=" + nodeMatch.isForNamedEntity());
        //    System.out.println("Token =" + token);
        // a może już w trakcie tworzenia tego obiektu wiemy ze nie jest kompatybilny z reszta wyniku...
        final PatternMatchSingleResult thisLevelResult = new PatternMatchSingleResult(_idsList, pmei, this.getRelationType());
        thisLevelResults.add(thisLevelResult);
      }

    } else {

      final PatternMatchExtraInfo pmei = new PatternMatchExtraInfo();
      pmei.setSentence(sentence);

      final LinkedHashSet<Integer> _idsList = new LinkedHashSet<>();
      _idsList.add(token.getNumberId());

//    System.out.println("_idsList = " + _idsList + " nmE=" + nodeMatch.isForNamedEntity());
//    System.out.println("Token =" + token);
      // a może już w trakcie tworzenia tego obiektu wiemy ze nie jest kompatybilny z reszta wyniku...
      thisLevelResults.add(new PatternMatchSingleResult(_idsList, pmei, this.getRelationType()));
    }


    final List<PatternMatchSingleResult> resultsToThisLevel = new ArrayList<>();
    /*
    for (final PatternMatchSingleResult thisLevelResult : thisLevelResults) {

      final List<PatternMatchSingleResult> validResults =
          output
              .stream()
              .filter(r -> r.haveNotCommonId(thisLevelResult))
              .map(PatternMatchSingleResult::new)
              .collect(Collectors.toList());

      validResults.forEach(r -> r.concatenateWith(thisLevelResult));
      resultsToThisLevel.addAll(validResults);
    }

    if (resultsToThisLevel.size() == 0) {
      return Collections.emptyList();
    }
    */

    if (nodeMatch.isLeaf()) {
      // final match!It is leaf so we end this branch of recursion here
      return thisLevelResults;
    }
    // we here know at this level there is a match. But there are further levels since this token is not a leaf ...

    // before we go any further let's check if already at current level there are contradictions between
    // thisLevel results and previous levels results


    final List<PatternMatchSingleResult> downLevelsResult = new LinkedList<>();
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

        final List<PatternMatchSingleResult> resultsForOnePermutation = getResultsForOnePermutation(resultsToThisLevel, sentence, childrenTokenPermutation, nodeMatch.getEdgeMatchList());
        downLevelsResult.addAll(resultsForOnePermutation);
      }
    }

    // jęsli żaden z "niższych" nie pasuje to ten zbiór do iteracji jest pusty i nawet jak aktualny pasuje
    // to nie ma się z czym skonkatenować i pusty wynik jest propagowany wyżej

    // filtrujemy te które nie mają nic nakładającego się z tym który chcemy dołączyć


    final List<PatternMatchSingleResult> totalResults = new ArrayList<>();

    for (final PatternMatchSingleResult thisLevelResult : thisLevelResults) {

      final List<PatternMatchSingleResult> validResults =
          downLevelsResult
              .stream()
              .filter(r -> r.haveNotCommonId(thisLevelResult))
              .map(PatternMatchSingleResult::new)
              .collect(Collectors.toList());

      validResults.forEach(r -> r.concatenateWith(thisLevelResult));
      totalResults.addAll(validResults);
    }

    return totalResults;
  }

  private final List<PatternMatchSingleResult>
  getResultsForOnePermutation(final List<PatternMatchSingleResult> output,
                              final Sentence sentence,
                              final List<Token> childrenTokenPermutation,
                              final List<EdgeMatch> patternEdges) {
    List<PatternMatchSingleResult> onePermutationResults = new ArrayList<>();

    for (int i = 0; i < patternEdges.size(); i++) {

      final EdgeMatch nextEdgeMatch = patternEdges.get(i);
      final Token nextToken = childrenTokenPermutation.get(i);

      final List<PatternMatchSingleResult> resultsForOnePatternEdge =
          getSentenceTreesMatchingGenericPatternFromTokenAndEdge(output, sentence, nextToken, nextEdgeMatch, nextEdgeMatch.getNodeMatch());

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
            // jeśli się nie nakładają idki
            if (oldPmsr.haveNotCommonId(pmsr)) {

              final PatternMatchSingleResult newPmsr = new PatternMatchSingleResult(oldPmsr);
              newPmsr.concatenateWith(pmsr);
              newOnePermutationResults.add(newPmsr);
            } else {
              System.out.println("NEW_ELIMINATION triggered");
            }
          }
        }

        onePermutationResults = newOnePermutationResults;
      }
    }
    return onePermutationResults;
  }

}
