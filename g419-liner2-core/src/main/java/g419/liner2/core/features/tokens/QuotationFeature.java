package g419.liner2.core.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

/**
 * Created by michal on 8/25/14.
 */
public class QuotationFeature extends TokenInSentenceFeature {

  public QuotationFeature(String name) {
    super(name);
  }

  @Override
  public void generate(Sentence sentence) {
    int thisFeatureIdx = sentence.getAttributeIndex().addAttribute(this.getName());
    boolean inQuotation = false;
    for (Token t : sentence.getTokens()) {
      if ("\"".equals(t.getOrth())) {
        if (inQuotation) {
          t.setAttributeValue(thisFeatureIdx, "E");
          inQuotation = false;
        } else {
          t.setAttributeValue(thisFeatureIdx, "B");
          inQuotation = true;
        }
      } else if (inQuotation) {
        t.setAttributeValue(thisFeatureIdx, "I");
      } else {
        t.setAttributeValue(thisFeatureIdx, "O");
      }
    }
  }
}
