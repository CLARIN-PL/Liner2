package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;

public class HasSymbolFeature extends ATokenFeature{
	
	private Pattern SYMBOLS = Pattern.compile("[\\p{Punct}\\p{P}\\p{S}\\p{Space}\\p{C}\\p{M}]");
	
	public HasSymbolFeature(String name){
		super(name);
	}
	
	public String generate(Token token){
		if (SYMBOLS.matcher(token.getAttributeValue(0)).find())
			return "1";
		else
			return "0";
	}

}
