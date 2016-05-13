package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.api.tools.TrieDictNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class IsAnnotationFeature extends TokenInSentenceFeature{

	private String type;

	public IsAnnotationFeature(String name, String type){
		super(name);
		this.type = type;
	}


	@Override
	public void generate(Sentence sentence){
		int thisFeatureIdx = sentence.getAttributeIndex().getIndex(this.getName());
		ArrayList<Token> tokens = sentence.getTokens();



		int tokenIdx = 0;
		while (tokenIdx < sentence.getTokenNumber()){
			tokens.get(tokenIdx).setAttributeValue(thisFeatureIdx,
					sentence.isChunkAt(tokenIdx, this.type) ? "1" : "0");
			tokenIdx++;
		}
	}

}
