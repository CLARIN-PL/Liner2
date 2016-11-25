package g419.crete.api.features.annotations;

import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;

public class AnnotationFeaturePreceedingRelativePronounDistance extends AbstractFeature<Annotation, Integer> {

	final static String KTORY_BASE = "który"; 
	
	@Override
	public void generateFeature(Annotation input) {
		this.value = 100;
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		List<Token> tokens = input.getSentence().getTokens();
		
		for(int i = 0; i < input.getBegin(); i++){
			Token token = tokens.get(i);
			String base = ai.getAttributeValue(token, "base");
			if(KTORY_BASE.equalsIgnoreCase(base))
				this.value = Math.min(this.value, input.getBegin() - tokens.indexOf(token));
		}
		
	}

	@Override
	public String getName() {
		return "annotation_preceeding_relative_pronoun_distance";
	}

	@Override
	public Class<Annotation> getInputTypeClass() {
		return Annotation.class;
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
