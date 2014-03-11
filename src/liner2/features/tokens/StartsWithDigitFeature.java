package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class StartsWithDigitFeature extends TokenFeature{
	
	private Pattern DIGITS = Pattern.compile("^\\p{N}");
	
	public StartsWithDigitFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (DIGITS.matcher(token.getAttributeValue(index.getIndex("orth"))).find())
			return "1";
		else
			return "0";
	}
}