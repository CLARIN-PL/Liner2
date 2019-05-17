package g419.liner2.core.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.List;


public class BaseNumberFeature extends TokenInSentenceFeature {

  public BaseNumberFeature(final String name) {
    super(name);
  }


  @Override
  public void generate(final Sentence sentence) {
    final int thisFeatureIdx = sentence.getAttributeIndex().getIndex(getName());
    final List<Token> tokens = sentence.getTokens();

    int tokenIdx = 0;
    while (tokenIdx < sentence.getTokenNumber()) {
      final Token t = tokens.get(tokenIdx);
      final String base = t.getAttributeValue("base");
      t.setAttributeValue(thisFeatureIdx, sentence.getDocument().getBaseCount(base) > 1 ? "1" : "0");
      tokenIdx++;
    }
  }

}
