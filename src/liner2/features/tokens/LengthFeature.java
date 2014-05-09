package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class LengthFeature extends TokenFeature{
		
	public LengthFeature(String name){
		super(name); 
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		return "" + t.getAttributeValue(index.getIndex("orth")).length();
	}


}
