package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class StartsWithSymbolFeature extends TokenFeature{
	
	private Pattern SYMBOLS = Pattern.compile("^[\\p{Punct}\\p{P}\\p{S}\\p{Space}\\p{C}\\p{M}]");
	
	public StartsWithSymbolFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (SYMBOLS.matcher(token.getAttributeValue(index.getIndex("orth"))).find())
			return "1";
		else
			return "0";
	}

}
