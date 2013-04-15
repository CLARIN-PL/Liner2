package liner2.features.tokens;

import liner2.structure.Token;

public class HasLowerFeature extends ATokenFeature{
	
	public HasLowerFeature(String name){
		super(name);
	}
	
	public String generate(Token token){
		for (char c: token.getAttributeValue(0).toCharArray()){
			if (Character.isLowerCase(c))
				return "1";
		}
		return "0";
	}
}