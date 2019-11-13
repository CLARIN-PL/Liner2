package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.*;
import java.util.Map.Entry;


public class ClassFeature extends TokenFeature {

  @SuppressWarnings("serial")
  public static final Map<String, Set<String>> BROAD_CLASSES = new HashMap<String, Set<String>>() {{
    put("verb", new HashSet<>(Arrays.asList(new String[] {"pact", "ppas", "winien", "praet", "bedzie", "fin", "impt", "ger", "imps", "inf"})));
    put("subst", new HashSet<>(Arrays.asList(new String[] {"subst", "depr", "xxs", "ger", "ppron12", "ppron3", "siebie"})));
    put("adv", new HashSet<>(Arrays.asList(new String[] {"pant", "pcon"})));
    put("pron", new HashSet<>(Arrays.asList(new String[] {"ppron12", "ppron3", "siebie"})));
  }};

  private boolean broad = false;

  private final ArrayList<String> possible_classes = new ArrayList<>(Arrays.asList("subst", "depr", "num", "numcol", "adj", "adja", "adjp", "adv", "ppron12",
      "ppron3", "siebie", "fin", "bedzie", "aglt", "praet", "impt", "imps",
      "inf", "pcon", "pant", "ger", "pact", "ppas", "winien", "pred", "prep",
      "conj", "qub", "xxs", "xxx", "ign", "interp"));


  public ClassFeature(final String name) {
    super(name);
  }

  public ClassFeature(final String name, final boolean broad) {
    super(name);
    this.broad = broad;
  }

  private String getTokenClass(final Token token, final TokenAttributeIndex index) {
    final String ctag = token.getAttributeValue(index.getIndex("ctag"));
    if (ctag != null) {
      for (final String val : ctag.split(":")) {
        if (this.possible_classes.contains(val)) {
          return val;
        }
      }
    }
    return null;
  }

  @Override
  public String generate(final Token token, final TokenAttributeIndex index) {
    String tokenClass = getTokenClass(token, index);
    if (tokenClass != null && this.broad) {
      for (final Entry<String, Set<String>> entry : BROAD_CLASSES.entrySet()) {
        if (entry.getValue().contains(tokenClass)) {
          tokenClass = entry.getKey();
          break;
        }
      }
    }
    return tokenClass;
  }


}