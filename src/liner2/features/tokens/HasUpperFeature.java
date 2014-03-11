package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class HasUpperFeature extends TokenFeature{
	
	public HasUpperFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		for (char c: token.getAttributeValue(0).toCharArray()){
			if (Character.isUpperCase(c))
				return "1";
		}
		return "0";
	}
}
