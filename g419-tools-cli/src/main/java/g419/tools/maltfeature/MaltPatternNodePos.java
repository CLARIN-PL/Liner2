package g419.tools.maltfeature;

import g419.liner2.core.tools.parser.MaltSentence;

public class MaltPatternNodePos extends MaltPatternNode {

  private String pos = null;

  public MaltPatternNodePos(final String label, final String pos) {
    super(label);
    this.pos = pos;
  }

  @Override
  public boolean check(final MaltSentence sentence, final int tokenIdx) {
    return sentence.getSentence().getTokens().get(tokenIdx).getDisambTag().getPos().equals(pos);
  }

}