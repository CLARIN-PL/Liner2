package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class HasDigitFeature extends TokenFeature{
	
	private Pattern DIGITS = Pattern.compile("\\p{N}");
	
	public HasDigitFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (DIGITS.matcher(token.getAttributeValue(0)).find())
			return "1";
		else
			return "0";
	}
}
