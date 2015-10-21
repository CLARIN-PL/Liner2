package g419.tools.maltfeature;

import g419.liner2.api.tools.parser.MaltSentence;

public class MaltPatternNodeBase extends MaltPatternNode{
	
	private String base = null;
	
	public MaltPatternNodeBase(String label, String base){
		super(label);
		this.base = base;
	}
	
	@Override
	public boolean check(MaltSentence sentence, int tokenIdx){
		return sentence.getSentence().getTokens().get(tokenIdx).getDisambTag().getBase().equals(this.base);
	}
	
}
