package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class HasSymbolFeature extends TokenFeature{
	
	private Pattern SYMBOLS = Pattern.compile("[\\p{Punct}\\p{P}\\p{S}\\p{Space}\\p{C}\\p{M}]");
	
	public HasSymbolFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (SYMBOLS.matcher(token.getAttributeValue(0)).find())
			return "1";
		else
			return "0";
	}

}
