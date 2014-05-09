package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class AllLettersFeature extends TokenFeature{
	
	private Pattern ALL_LETTERS = Pattern.compile("^\\p{L}+$");
	
	public AllLettersFeature(String name){
		super(name); 
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		if (ALL_LETTERS.matcher(orth).find())
			return "1";
		else
			return "0";
		
	}


}
