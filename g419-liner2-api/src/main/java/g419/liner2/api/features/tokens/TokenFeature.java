package g419.liner2.api.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public abstract class TokenFeature extends Feature{

	public TokenFeature(String name) {
		super(name);
	}

	public abstract String generate(Token token, TokenAttributeIndex index);
	
}
