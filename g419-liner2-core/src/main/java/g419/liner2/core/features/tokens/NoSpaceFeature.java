package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class NoSpaceFeature extends TokenFeature{
	
	public NoSpaceFeature(){
		super("nospace");
	}
	
	@Override
	public String generate(Token t, TokenAttributeIndex index){
		return t.getNoSpaceAfter() ? "0" : "1";
	}	
	
}
