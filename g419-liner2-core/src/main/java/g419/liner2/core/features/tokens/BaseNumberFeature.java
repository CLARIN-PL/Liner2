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

  private HashMap<String, Integer> bases = null;


  private void countBasesFrequency(final Document document){
    for (final Paragraph p : document.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          final String lemma = t.getAttributeValue("base");
          if (this.bases.containsKey(lemma)) {
            this.bases.put(lemma, bases.get(lemma) + 1);
          } else {
            this.bases.put(lemma, 1);
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
          t.setAttributeValue(thisFeatureIdx, this.bases.get(base) > 1 ? "1" : "0");
        }
      }
    }
    this.bases = null;
  }
}
