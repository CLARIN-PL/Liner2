package liner2.features.tokens;

import liner2.structure.Token;

public class StartsWithUpperFeature extends TokenFeature{
	
	public StartsWithUpperFeature(String name){
		super(name);
	}
	
	public String generate(Token token){
		if (Character.isUpperCase(token.getAttributeValue(0).charAt(0)))
			return "1";
		else
			return "0";
	}
}
	

