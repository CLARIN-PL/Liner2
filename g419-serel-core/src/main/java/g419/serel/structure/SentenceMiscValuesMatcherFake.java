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
public class SentenceMiscValuesMatcherFake {


  Sentence sentence;
  int sentenceIndex;
  List<Map<String, Object>> miscValuesList = new ArrayList<>();


  public static void enhanceTokensWithMiscStructures(final Sentence s) {

  }


  public static SentenceMiscValuesMatcherFake from(final Sentence s, final int _sentenceIndex) {

    final SentenceMiscValuesMatcherFake smv = new SentenceMiscValuesMatcherFake();
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

        // odsiewamy te, które są zagnieżdżone
        rs.stream().forEach(rs_instance -> rs_instance.setSentence(this.sentence));
        rels.addAll(rs.stream().filter(RelationDesc::isNotNested).collect(Collectors.toList()));
      }
    }
    return rels;
  }

}
