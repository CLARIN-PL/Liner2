package g419.corpus.structure;

//import g419.serel.ruleTree.PatternMatch;

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
    s.getNamRels().forEach(namRels -> namRels.setSentence(s));
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
