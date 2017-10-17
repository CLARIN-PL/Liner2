package g419.tools.maltfeature;

import g419.liner2.core.tools.parser.MaltSentence;

public class MaltPatternNodeAny extends MaltPatternNode{
	
	public MaltPatternNodeAny(String label){
		super(label);
	}
	
	@Override
	public boolean check(MaltSentence sentence, int tokenIdx){
		return true;
	}
	
}
