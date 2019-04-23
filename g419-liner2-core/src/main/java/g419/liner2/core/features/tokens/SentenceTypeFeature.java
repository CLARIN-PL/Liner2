package g419.liner2.core.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.List;


public class SentenceTypeFeature extends TokenInSentenceFeature {

  public SentenceTypeFeature(final String name) {
    super(name);
  }


  @Override
  public void generate(final Sentence sentence) {
    final int thisFeatureIdx = sentence.getAttributeIndex().getIndex(getName());
    final List<Token> tokens = sentence.getTokens();
    final String sentenceId = sentence.getId();

    int tokenIdx = 0;
    while (tokenIdx < sentence.getTokenNumber()) {
      final Token t = tokens.get(tokenIdx);
      t.setAttributeValue(thisFeatureIdx, sentenceId);
      tokenIdx++;
    }
  }

}
