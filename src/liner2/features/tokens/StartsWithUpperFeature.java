package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class StartsWithUpperFeature extends TokenFeature{
	
	public StartsWithUpperFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (Character.isUpperCase(token.getAttributeValue(index.getIndex("orth")).charAt(0)))
			return "1";
		else
			return "0";
	}
}
	

