package g419.liner2.core.features.tokens;


import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.Paragraph;

import java.util.HashMap;

public class BaseNumberFeature extends TokenInDocumentFeature {

  public BaseNumberFeature(String name) {
    super(name);
  }

  private HashMap<String, Integer> baseCount = null;

  public int getBaseCount(final String base){
    return this.baseCount.get(base);
  }

  private void countBasesFrequency(final Document document){
    for (final Paragraph p : document.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          final String lemma = t.getAttributeValue("base");
          if (this.baseCount.containsKey(lemma)) {
            this.baseCount.put(lemma, baseCount.get(lemma) + 1);
          } else {
            this.baseCount.put(lemma, 1);
          }
        }
      }
    }
  }

  @Override
  public void generate(final Document document) {

    final int thisFeatureIdx = document.getAttributeIndex().getIndex(getName());

    this.countBasesFrequency(document);

    for (final Paragraph p : document.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          final String base = t.getAttributeValue("base");
          t.setAttributeValue(thisFeatureIdx, this.baseCount.get(base) > 1 ? "1" : "0");
        }
      }
    }
  }
}
