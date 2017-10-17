package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class HasSymbolFeature extends TokenFeature{
	
	private Pattern SYMBOLS = Pattern.compile("[\\p{Punct}\\p{P}\\p{S}\\p{Space}\\p{C}\\p{M}]");
	
	public HasSymbolFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (SYMBOLS.matcher(token.getAttributeValue(index.getIndex("orth"))).find())
			return "1";
		else
			return "0";
	}

}
