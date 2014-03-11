package liner2.features.tokens;

import liner2.structure.Sentence;

public abstract class TokenInSentenceFeature extends Feature{

	public TokenInSentenceFeature(String name) {
		super(name);
	}

	public abstract void generate(Sentence sentence);
	
}
