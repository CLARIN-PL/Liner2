package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

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