package liner2.features.tokens;

import liner2.structure.Token;

public abstract class TokenFeature extends Feature{

	public TokenFeature(String name) {
		super(name);
	}

	public abstract String generate(Token token);
	
}
