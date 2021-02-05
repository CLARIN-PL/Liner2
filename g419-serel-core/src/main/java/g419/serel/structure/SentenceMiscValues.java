package g419.serel.structure;

import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.serel.ruleTree.PatternMatch;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public class SentenceMiscValues {


  Sentence sentence;
  int sentenceIndex;
  List<Map<String, Object>> miscValuesList = new ArrayList<>();


  public static void enhanceTokensWithMiscStructures(final Sentence s) {

  }


  public static SentenceMiscValues from(final Sentence s, final int _sentenceIndex) {

    final SentenceMiscValues smv = new SentenceMiscValues();
    smv.setSentence(s);
    smv.setSentenceIndex(_sentenceIndex);
    smv.readMiscValues(s);
    return smv;
  }

  public void readMiscValues(final Sentence sentence) {

    for (int index = 0; index < sentence.getTokens().size(); index++) {
      final Token token = sentence.getTokens().get(index);
      //log.debug(" ---- token index = " + index + "  ------");

      if (token.getAttributeIndex().getIndex("misc") != -1) {
        final String misc = token.getAttributeValue("misc");
        //log.debug("misc = " + misc);
        if (!misc.equals("_")) {
          ////System.out.println("HAve misc!");
          // misc = misc.replaceAll("'","\"");
          final Map<String, Object> result = getMapFromMiscColumn(misc);
          token.extAttr = result;
          //System.out.println("result ="+result);
          miscValuesList.add(result);
        }
      }
    }
  }


  private Map<String, Object> getMapFromMiscColumn(final String misc) {
    final Map result = new HashMap<String, Object>();

    //log.debug("misc = " + misc);

    String key = null;
    for (int i = 0; i < misc.length(); i++) {
      //log.debug("chk i = " + i);
      if (misc.charAt(i) == '\'') {
        final String textValue = getTextValue(i + 1, misc);
        //log.debug("textValue =" + textValue);
        if (key == null) {
          key = textValue;
        } else {
          switch (key) {
            case "bois":
              result.put(key, getBoisValue(textValue));
              break;
            case "nam_rels":
              if (textValue.length() > 2) {  // są puste artefakty [] powstające z relacji między róznymi zdaniami
                result.put(key, getNamRelsValue(textValue));
              }
              break;

          }
          key = null;
        }
        i += (textValue.length() + 1);
      }
    }
    //log.debug("koniec preprocessingu misc");
    return result;
  }

  private String getTextValue(final int i, final String s) {
    final int index = s.indexOf('\'', i);
    return s.substring(i, index);
  }

  private List<String> getBoisValue(String s) {
    s = s.substring(1, s.length() - 1);
    final String[] splits = s.split(",");
    return Arrays.asList(splits);
  }

  private List<RelationDesc> getNamRelsValue(String s) {
    s = s.substring(1, s.length() - 1);
    //log.debug("getNamRelsVAlue: " + s);
    final String[] splits = s.split(",");

    final List<RelationDesc> results = Arrays.stream(splits).map(RelationDesc::from).collect(Collectors.toList());
    results.stream().forEach(rd -> {
      rd.setSentence(sentence);
      rd.setSentenceIndex(this.sentenceIndex);
    });
    return results;
  }


  // przejdź po tokenach zdania i sprawdzaj czy pasuje
  //// jest token dla którego pasuje :
  ////// idź w górę i rozpoznawaj aż dojdziesz do korzenia wzorca
  //////// jak dojdziesz do końca gałęzi i jest ok to masz korzeń:
  ////////// teraz możesz sprawdzić czy druga (następna) gałąź pasuje w coś od korzenia w dół
  ////// gdzieś po drodze się nie zgadza -> null
  ////// zabrakło tokenów dla wzorca -> null
  //// nie ma takiego tokena - null;



/*
  public Set<RelationDesc> getTokenIndexesForMatchingRelType(final PatternMatch rmr, final Set<RelationDesc> useOnlyRels) {
    //System.out.println("Searching for type '"+rmr.getRelationType()+"'");
    final Set<Integer> tokenIndexes = new HashSet<>();
    final Set<RelationDesc> resultRelDesc = new HashSet<>();
    for (int i = 0; i < miscValuesList.size(); i++) {

      final Map<String, Object> map = miscValuesList.get(i);
      final List<RelationDesc> relDescs = (List<RelationDesc>) map.get("nam_rels");
      if (relDescs != null) {
        for (final RelationDesc relDesc : relDescs) {

          if ((useOnlyRels != null) && !useOnlyRels.contains(relDesc)) {
            continue;
          }


          //System.out.println("relDesc.type = '"+relDesc.getType()+"'");
          if (relDesc.getType().equals(rmr.getRelationType())) {
            tokenIndexes.add(i);
            relDesc.setSentence(this.sentence);
            resultRelDesc.add(relDesc);
          } else {
            //System.out.println("Rel " + relDesc + " skipped because is of type");
          }
        }
      }
    }
    //return tokenIndexes;
    return resultRelDesc;
  }


  public Set<SerelExpression> getRelationsMatchingRule(final PatternMatch rmr, final List<SerelExpression> sentenceLinksList) {
    final Set<SerelExpression> result = new HashSet<>();
    for (final SerelExpression se : sentenceLinksList) {
      if (isRuleMatchingSerel(rmr, se)) {
        result.add(se);
      }
    }
    return result;
  }


  private boolean isRuleMatchingSerel(final PatternMatch rmr, final SerelExpression se) {

    final String serelPath = se.getDetailedPathAsString(false);
    //System.out.println("sePAth = "+serelPath);

    final StringTokenizer tokenizer = new StringTokenizer(serelPath, ">|<", true);
    final List<String> serelPathElements = new ArrayList<>();
    while (tokenizer.hasMoreElements()) {
      serelPathElements.add(tokenizer.nextToken().trim());
    }

    //System.out.println("seTokens size = "+serelPathElements.size());
    //System.out.print("seTokens= "); serelPathElements.forEach(p->System.out.print(p+" , "));
    //System.out.println("");


    for (int serelPathElementIndex = 0; serelPathElementIndex < serelPathElements.size(); serelPathElementIndex++) {
      if (isRuleMatchingSerelPathFromTokenIndex(rmr, se, serelPathElements, serelPathElementIndex)) {
        return true;
      }
    }
    return false;
  }


  private boolean isRuleMatchingSerelPathFromTokenIndex(final PatternMatch rmr, final SerelExpression se, final List<String> serelPathElements, int serelPathElementIndex) {

    //System.out.println("Rule :"+ rmr.rule);
    for (int ruleElementIndex = 0; ruleElementIndex < rmr.getRuleElements().size(); ruleElementIndex++, serelPathElementIndex++) {
      //System.out.println("Checking ruleElementIndex="+ruleElementIndex+ " serelPathElementIndex ="+serelPathElementIndex);
      final String rElement = rmr.getRuleElements().get(ruleElementIndex).trim();
      //System.out.println("rElement ="+rElement);
      if (rElement.equals("*")) {
        //System.out.println("* found");
        final int resIndex = matchStarToSerelPath(rmr, se, serelPathElements, serelPathElementIndex, ruleElementIndex);
        //System.out.println("matched * returned resIndex = "+resIndex);
        //System.out.println("");
        //System.out.println("");
        if (resIndex == -1) {
          return false;
        }
        serelPathElementIndex = resIndex;
        ruleElementIndex = ruleElementIndex + 2;
        continue;
      }
      if (ruleElementIndex % 2 == 1) {
        //System.out.println(" START : %2==1 skip");
        final String sElement = serelPathElements.get(serelPathElementIndex).trim();
        if (!rElement.equals(sElement)) {
          return false;
        }
        //System.out.println(" END : %2==1 skip. ruleElementIndex="+ruleElementIndex+ " serelPathElementIndex="+serelPathElementIndex);
        continue;
      }
      // może to przenieść do samej góry ?
      if ((ruleElementIndex == rmr.sourceRuleElementIndex) && (serelPathElementIndex == se.getParents1().get(0).getSourceIndex())) {
        //System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") Match! Source. ");
        continue;
      }
      if ((ruleElementIndex == rmr.targetRuleElementIndex) && (serelPathElementIndex == se.getParents2().get(0).getSourceIndex())) {
        //System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") Match! Target. ");
        continue;
      }

      final boolean areTheSame = rmr.isRuleElementMatchingSerelPathElement(ruleElementIndex, serelPathElements, serelPathElementIndex, se);
      if (!areTheSame) {
        //System.out.println("(" + serelPathElementIndex + ":" + ruleElementIndex + ") No match! RuleElement= " + rmr.getRuleElements().get(ruleElementIndex) + " PathElement = " + serelPathElements.get(serelPathElementIndex));
        return false;
      }
      //System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") Match! Token= "+ sentence.getTokens().get(serelPathElementIndex)+" ruleElement="+rmr.getRuleElements().get(ruleElementIndex));
    }
    return true;
  }


  private int matchStarToSerelPath(final PatternMatch rmr, final SerelExpression se, final List<String> serelPathElements, int serelPathElementIndex, final int ruleElementIndexParam) {

    //System.out.println("");
    //System.out.println("");
    //System.out.println("matchStarToSerelPath. ruleElementIndex = "+ruleElementIndexParam);

    final int ruleElementIndex = ruleElementIndexParam + 2;
    final String rElementText = rmr.getRuleElements().get(ruleElementIndex).trim();
    final String rElementDepRel = rmr.getRuleElementsDeprel().get(ruleElementIndex).trim();

    //System.out.println(" STARMODE: retext ="+ruleElementIndex+ " reDeprel ="+ rmr.ruleElementsDeprel);
    //System.out.println("");
    //System.out.println("");

    for (final int i = serelPathElementIndex; serelPathElementIndex < serelPathElements.size(); serelPathElementIndex++) {

      //System.out.println("*: serelPathElementIndex ="+serelPathElementIndex);

      if (i % 2 == 1) {
        continue;
      }

      final boolean areTheSame = rmr.isRuleElementMatchingSerelPathElement(ruleElementIndex, serelPathElements, serelPathElementIndex, se);

      if (areTheSame) {
        return serelPathElementIndex;
      }
    }

    return -1;

  }


  public Set<RelationDesc> getTokenIndexesForMatchingRelNE(final PatternMatch rmr, final Set<RelationDesc> useOnlyRels) {
    //System.out.println("Searching for NE types (and deprel if given): "+rmr);
    final Set<Integer> tokenIndexes = new HashSet<>();
    final Set<RelationDesc> resultRelDesc = new HashSet<>();

    final String fromNEType = rmr.getSourceEntityName();
    final String toNEType = rmr.getTargetEntityName();

    for (int i = 0; i < miscValuesList.size(); i++) {
      final Map<String, Object> map = miscValuesList.get(i);
      final List<RelationDesc> relDescs = (List<RelationDesc>) map.get("nam_rels");
      if (relDescs != null) {
        for (final RelationDesc relDesc : relDescs) {

          if ((useOnlyRels != null) && !useOnlyRels.contains(relDesc)) {
            continue;
          }


          if ((relDesc.getFromType().equals(fromNEType)) &&
              (relDesc.getToType().equals(toNEType))) {

            final String ruleSourceDeprel = rmr.getRuleElementsDeprel().get(rmr.getSourceRuleElementIndex());
            if ((ruleSourceDeprel != null) && (!ruleSourceDeprel.isEmpty())) {
              final String tokenSourceDeprel = sentence.getTokens().get(relDesc.getFromTokenIndex() - 1).getAttributeValue("deprel");
              //System.out.println("rSource='" + ruleSourceDeprel + "'  tSource='" + tokenSourceDeprel + "'");
              if (!ruleSourceDeprel.equals(tokenSourceDeprel)) {
                //System.out.println("Rel " + relDesc + " skipped because of source deprel");
                continue;
              }
            }

            final String ruleTargetDeprel = rmr.getRuleElementsDeprel().get(rmr.getTargetRuleElementIndex());
            if ((ruleTargetDeprel != null) && (!ruleTargetDeprel.isEmpty())) {
              final String tokenTargetDeprel = sentence.getTokens().get(relDesc.getToTokenIndex() - 1).getAttributeValue("deprel");
              //System.out.println("rTarget='" + ruleTargetDeprel + "'  tTarget='" + tokenTargetDeprel + "'");

              if (!ruleTargetDeprel.equals(tokenTargetDeprel)) {
                //System.out.println("Rel " + relDesc + " skipped because of target deprel");
                continue;
              }
            }

            tokenIndexes.add(i);
            relDesc.setSentence(this.sentence);
            resultRelDesc.add(relDesc);
          } else {
            //System.out.println("Rel " + relDesc + " skipped because of NETypes");
          }
        }
      }
    }
    //return tokenIndexes;
    return resultRelDesc;
  }
*/


  public List<RelationDesc> getRelationsMatchingPatternTypeAndAnnotations(final PatternMatch patternMatch) {

    final List<RelationDesc> matchingRels = new LinkedList<>();
    final List<String> leavesAnnotationType = patternMatch.getAllAnnotations();
    final List<RelationDesc> allRels = getAllNamRels();

    for (final RelationDesc relDesc : allRels) {
      if (relDesc.getType().equals(patternMatch.getRelationType())) {

        // TODO : here - when relation will be more than binary - one needs to handle it additionally (all combinations)
        final String fromAnnotationType = relDesc.getFromType();
        final String toAnnotationType = relDesc.getToType();

        if (
            (fromAnnotationType.equals(leavesAnnotationType.get(0)) && toAnnotationType.equals(leavesAnnotationType.get(1)))
                ||
                (toAnnotationType.equals(leavesAnnotationType.get(0)) && fromAnnotationType.equals(leavesAnnotationType.get(1)))
        ) {
//          System.out.println("adding relDesc = " + relDesc);
          matchingRels.add(relDesc);
        }
      }
    }

    return matchingRels;
  }


  public List<RelationDesc> getRelationsMatchingPatternType(final PatternMatch patternMatch) {

    final List<RelationDesc> matchingRels = new LinkedList<>();
    //final List<String> leavesAnnotationType = patternMatch.getAllAnnotations();
    final List<RelationDesc> allRels = getAllNamRels();

    for (final RelationDesc relDesc : allRels) {
      if (relDesc.getType().equals(patternMatch.getRelationType())) {
        matchingRels.add(relDesc);
      }
    }
    return matchingRels;
  }


  public List<RelationDesc> getAllNamRels() {
    final List<RelationDesc> rels = new LinkedList<>();

    for (final Map map : this.getMiscValuesList()) {
      if (map.containsKey("nam_rels")) {
        final List<RelationDesc> rs = (List<RelationDesc>) map.get("nam_rels");
        rels.addAll(rs);
      }
    }
    return rels;
  }

}
