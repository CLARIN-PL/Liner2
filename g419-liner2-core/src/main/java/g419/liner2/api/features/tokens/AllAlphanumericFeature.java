package g419.liner2.api.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;


public class AllAlphanumericFeature extends TokenFeature{
	
	private Pattern ALL_ALPHANUM = Pattern.compile("^[\\p{L}\\p{N}]+$");
	
	public AllAlphanumericFeature(String name){
		super(name); 
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		if (ALL_ALPHANUM.matcher(orth).find())
			return "1";
		else
			return "0";
		
	}


}
