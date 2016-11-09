package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;


public class TfIdfFeature extends TokenInSentenceFeature{

	public TfIdfFeature(String name){
		super(name);
	}


	@Override
	public void generate(Sentence sentence){

		int thisFeatureIdx = sentence.getAttributeIndex().getIndex(this.getName());
		ArrayList<Token> tokens = sentence.getTokens();

		float tokenNumber = (float)(sentence.getDocument().getTokenNumber());

		int tokenIdx = 0;
		while (tokenIdx < sentence.getTokenNumber()) {
			Token t = tokens.get(tokenIdx);
			String base = t.getAttributeValue("base");
			float idf = Float.parseFloat(t.getAttributeValue("key_value_idf_base"));
			float tf = (float)(sentence.getDocument().getBaseCount(base));
			float normTf = tf / tokenNumber;
			float tfIdf = normTf * idf;
			t.setAttributeValue(thisFeatureIdx, tfIdf > 0.05 ? "1" : "0");
			//String.format("%.2f", tfIdf)
			tokenIdx++;
		}
	}

}
