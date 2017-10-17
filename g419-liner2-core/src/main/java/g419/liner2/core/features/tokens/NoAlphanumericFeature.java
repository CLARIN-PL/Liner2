package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class NoAlphanumericFeature extends TokenFeature{
	
	private Pattern ANY_ALPHANUMERIC = Pattern.compile("[\\p{L}\\p{N}]");
	
	public NoAlphanumericFeature(String name){
		super(name); 
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		if (ANY_ALPHANUMERIC.matcher(orth).find())
			return "0";
		else
			return "1";
		
	}


}
