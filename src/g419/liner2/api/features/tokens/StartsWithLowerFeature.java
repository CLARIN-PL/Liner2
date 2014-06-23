package g419.liner2.api.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class StartsWithLowerFeature extends TokenFeature{
	
	public StartsWithLowerFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (Character.isLowerCase(token.getAttributeValue(index.getIndex("orth")).charAt(0)))
			return "1";
		else
			return "0";
	}
}