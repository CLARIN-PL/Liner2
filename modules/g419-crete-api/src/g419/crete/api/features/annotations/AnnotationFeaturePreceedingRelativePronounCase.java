package g419.crete.api.features.annotations;

import java.util.ArrayList;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.enumvalues.Case;

public class AnnotationFeaturePreceedingRelativePronounCase extends AbstractFeature<Annotation, Case>{
	
	final static String KTORY_BASE = "który";
	
	@Override
	public void generateFeature(Annotation input) {
		this.value  = Case.OTHER;
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		ArrayList<Token> tokens = input.getSentence().getTokens();
		
		for(int i = 0; i < input.getBegin(); i++){
			Token token = tokens.get(i);
			String base = ai.getAttributeValue(token, "base");
			if(KTORY_BASE.equalsIgnoreCase(base))
				this.value = Case.fromValue(ai.getAttributeValue(token, "case"));
		}		
	}

	@Override
	public String getName() {
		return "annotation_preceeding_relative_pronoun_case";
	}

	@Override
	public Class<Annotation> getInputTypeClass() {
		return Annotation.class;
	}

	@Override
	public Class<Case> getReturnTypeClass() {
		return Case.class;
	}
	
	
	
	
}
