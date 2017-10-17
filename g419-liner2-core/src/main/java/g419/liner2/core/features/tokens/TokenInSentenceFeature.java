package g419.liner2.core.features.tokens;

import g419.corpus.structure.Sentence;

public abstract class TokenInSentenceFeature extends Feature{

	public TokenInSentenceFeature(String name) {
		super(name);
	}

	public abstract void generate(Sentence sentence);
	
}
