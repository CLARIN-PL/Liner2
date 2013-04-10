package liner2.features.tokens;

import liner2.structure.Token;

public abstract class ATokenFeature {

	public String name;
	
	public ATokenFeature(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public abstract String generate(Token token);
	
}
