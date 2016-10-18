package g419.liner2.api.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class HasUpperFeature extends TokenFeature{
	
	public HasUpperFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		for (char c: token.getAttributeValue(index.getIndex("orth")).toCharArray()){
			if (Character.isUpperCase(c))
				return "1";
		}
		return "0";
	}
}
