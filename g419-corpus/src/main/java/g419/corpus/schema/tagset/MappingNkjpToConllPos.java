package g419.corpus.schema.tagset;

import com.google.common.collect.Maps;

import java.util.Map;

public class MappingNkjpToConllPos {

  final static private Map<String, String> mapping = Maps.newHashMap();

  static {
    mapping.put("bedzie", "verb");
    mapping.put("fin", "verb");
    mapping.put("imps", "verb");
    mapping.put("impt", "verb");
    mapping.put("inf", "verb");
    mapping.put("praet", "verb");
    mapping.put("pred", "verb");
    mapping.put("winien", "verb");
    mapping.put("subst", "subst");
    mapping.put("depr", "subst");
    mapping.put("ger", "subst");
    mapping.put("ppron12", "subst");
    mapping.put("ppron3", "subst");
    mapping.put("siebie", "subst");
    mapping.put("adj", "adj");
    mapping.put("adja", "adj");
    mapping.put("adjc", "adj");
    mapping.put("adjp", "adj");
    mapping.put("pact", "adj");
    mapping.put("ppas", "adj");
    mapping.put("adv", "adv");
    mapping.put("pant", "adv");
    mapping.put("pcon", "adv");
    mapping.put("aglt", "aglt");
    mapping.put("brev", "brev");
    mapping.put("burk", "burk");
    mapping.put("comp", "comp");
    mapping.put("conj", "conj");
    mapping.put("ign", "ign");
    mapping.put("interj", "interj");
    mapping.put("interp", "interp");
    mapping.put("num", "num");
    mapping.put("numcol", "numcol");
    mapping.put("prep", "prep");
    mapping.put("qub", "qub");
    mapping.put("xxx", "xxx");
  }

  public static Map<String, String> get() {
    return MappingNkjpToConllPos.mapping;
  }
}
