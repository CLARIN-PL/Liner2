package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class AllUpperFeature extends TokenFeature{
	
	private Pattern ALL_UPPER = Pattern.compile("^\\p{Lu}+$");
	
	public AllUpperFeature(String name){
		super(name); 
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		if (ALL_UPPER.matcher(orth).find())
			return "1";
		else
			return "0";
		
	}


}
