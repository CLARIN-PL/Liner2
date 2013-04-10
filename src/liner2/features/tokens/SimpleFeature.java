package liner2.features.tokens;

import liner2.structure.Token;

public class SimpleFeature extends ATokenFeature{

	private String name = null;
	
	public SimpleFeature(String name){
		super(name);
		this.name = name;
	}
	
	
	public String generate(Token token){
		return "SimpleGenerator";
	}
	
}