package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class SimpleFeature extends TokenFeature{

	private String name = null;
	
	public SimpleFeature(String name){
		super(name);
		this.name = name;
	}
	
	
	public String generate(Token token, TokenAttributeIndex index){
		return "SimpleGenerator";
	}
	
}