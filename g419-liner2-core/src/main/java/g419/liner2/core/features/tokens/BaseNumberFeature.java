package g419.liner2.core.features.tokens;

import java.util.List;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;


public class BaseNumberFeature extends TokenInSentenceFeature{

	public BaseNumberFeature(String name){
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
			t.setAttributeValue(thisFeatureIdx, sentence.getDocument().getBaseCount(base) > 1 ? "1" : "0");
			tokenIdx++;
		}
	}

}
