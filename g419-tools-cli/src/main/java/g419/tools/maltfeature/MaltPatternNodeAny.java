package g419.tools.maltfeature;

import g419.liner2.core.tools.parser.MaltSentence;

public class MaltPatternNodeAny extends MaltPatternNode {

  public MaltPatternNodeAny(final String label) {
    super(label);
  }

  @Override
  public boolean check(final MaltSentence sentence, final int tokenIdx) {
    return true;
  }

}
