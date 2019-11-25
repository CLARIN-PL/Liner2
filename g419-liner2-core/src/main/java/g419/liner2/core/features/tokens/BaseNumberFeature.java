package g419.liner2.core.features.tokens;

import g419.liner2.core.tools.BaseCount;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.Paragraph;

import java.util.HashMap;

public class BaseNumberFeature extends TokenInDocumentFeature {

  public BaseNumberFeature(String name) {
    super(name);
  }

  @Override
  public void generate(final Document document) {
    BaseCount baseCount = new BaseCount(document);
    final int thisFeatureIdx = document.getAttributeIndex().getIndex(getName());

    for (final Paragraph p : document.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          t.setAttributeValue(thisFeatureIdx, baseCount.getBaseCount(t) > 1 ? "1" : "0");
        }
      }
    }
  }
}
