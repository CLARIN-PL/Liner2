package g419.tools.maltfeature;

import g419.liner2.core.tools.parser.MaltSentence;

public class MaltPatternNodePos extends MaltPatternNode{
	
	private String pos = null;
	
	public MaltPatternNodePos(String label, String pos){
		super(label);
		this.pos = pos;
	}
	
	@Override
	public boolean check(MaltSentence sentence, int tokenIdx){
		return sentence.getSentence().getTokens().get(tokenIdx).getDisambTag().getPos().equals(this.pos);
	}
	
}