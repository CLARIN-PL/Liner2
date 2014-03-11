package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class HasLowerFeature extends TokenFeature{
	
	public HasLowerFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		for (char c: token.getAttributeValue(0).toCharArray()){
			if (Character.isLowerCase(c))
				return "1";
		}
		return "0";
	}
}