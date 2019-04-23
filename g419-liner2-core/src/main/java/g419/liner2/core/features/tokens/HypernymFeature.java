package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataLemmaRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

import java.util.ArrayList;
import java.util.Collections;

public class HypernymFeature extends TokenFeature {

  WordnetLoader database;
  private int distance;

  public HypernymFeature(String name, WordnetLoader database, int distance) {
    super(name);
    this.database = database;
    this.distance = distance;
  }

  public String generate(Token token, TokenAttributeIndex index) {
    String base = token.getAttributeValue(index.getIndex("base"));
    ArrayList<String> lemmas = new ArrayList<String>();
    ArrayList<PrincetonDataRaw> thisDistSynsets = database.getSynsets(base);
    ArrayList<PrincetonDataRaw> allSynsets = new ArrayList<PrincetonDataRaw>();

    for (int i = 0; i < distance; i++) {
      ArrayList<PrincetonDataRaw> nextDistSynsets = new ArrayList<PrincetonDataRaw>();
      for (PrincetonDataRaw synset : thisDistSynsets) {
        nextDistSynsets.addAll(database.getHypernyms(synset));
      }
      allSynsets.addAll(nextDistSynsets);
      thisDistSynsets = nextDistSynsets;
    }
    if (allSynsets.isEmpty()) {
      return base;
    }
    for (PrincetonDataLemmaRaw lr : allSynsets.get(0).lemmas) {
      lemmas.add(lr.lemma);
    }
    if (allSynsets.size() > 1) {

      for (PrincetonDataRaw synset : allSynsets.subList(1, allSynsets.size())) {
        ArrayList<String> common_lemmas = new ArrayList<String>();
        for (PrincetonDataLemmaRaw lr : synset.lemmas) {
          if (lemmas.contains(lr.lemma)) {
            common_lemmas.add(lr.lemma);
          }
        }
        lemmas = common_lemmas;
      }
    }

    if (lemmas.isEmpty()) {
      return base;
    }
    Collections.sort(lemmas);
    return lemmas.get(0);

  }
}
