package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class NoLettersFeature extends TokenFeature{
	
	private Pattern ANY_LETTER = Pattern.compile("\\p{L}");
	
	public NoLettersFeature(String name){
		super(name); 
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		if (ANY_LETTER.matcher(orth).find())
			return "0";
		else
			return "1";
		
	}


}
