package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;


public class ParagraphTypeFeature extends TokenInSentenceFeature{

	public ParagraphTypeFeature(String name){
		super(name);
	}


	@Override
	public void generate(Sentence sentence){
		int thisFeatureIdx = sentence.getAttributeIndex().getIndex(this.getName());
		ArrayList<Token> tokens = sentence.getTokens();
		String paragraphType = sentence.getParagraph().getChunkMetaData("type");


		int tokenIdx = 0;
		while (tokenIdx < sentence.getTokenNumber()){
			tokens.get(tokenIdx).setAttributeValue(thisFeatureIdx, paragraphType);
			tokenIdx++;
		}
	}

}
