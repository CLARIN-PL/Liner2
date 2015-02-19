package g419.crete.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;

import java.util.ArrayList;

public class AnnotationFeatureFirstVerbInSentence extends AbstractFeature<Annotation, Boolean> {

	final String VERB_POS = "verb";
	
	@Override
	public void generateFeature(Annotation input) {
		this.value = false;
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		ArrayList<Token> sentenceTokens = input.getSentence().getTokens();
		int inputIndex = input.getBegin();
		for(int currIndex = 0; currIndex < inputIndex; currIndex++){
			Token currentToken = sentenceTokens.get(currIndex);
			if(VERB_POS.equals(ai.getAttributeValue(currentToken, "pos")) || VERB_POS.equals(ai.getAttributeValue(currentToken, "class")))
				this.value = true;
		}
	}

	@Override
	public String getName() {
		return "annotation_first_verb_in_sentence";
	}

	@Override
	public Class<Annotation> getInputTypeClass() {
		return Annotation.class;
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
