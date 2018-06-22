package g419.liner2.core.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.List;


public class TokenNumberFeature extends TokenInSentenceFeature{

	public TokenNumberFeature(String name){
		super(name);
	}


	@Override
	public void generate(Sentence sentence){
		int thisFeatureIdx = sentence.getAttributeIndex().getIndex(this.getName());
		List<Token> tokens = sentence.getTokens();

		int tokenIdx = 0;
		while (tokenIdx < sentence.getTokenNumber()) {
			Token t = tokens.get(tokenIdx);
			String base = t.getAttributeValue("base");
			t.setAttributeValue(thisFeatureIdx, tokenIdx + "");
			tokenIdx++;
		}
	}

}
