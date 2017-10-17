package g419.crete.core.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.enumvalues.Number;

import java.util.Arrays;
import java.util.List;

public class AnnotationFeatureNumber extends AbstractFeature<Annotation, Number>{

	@Override
	public void generateFeature(Annotation input) {
		input.assignHead();
		Token headToken = input.getSentence().getTokens().get(input.getHead());
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		
		this.value = Number.fromValue(ai.getAttributeValue(headToken, "number"));
	}

	@Override
	public int getSize() {
		return Number.values().length;
	}

	@Override
	public List<Number> getAllValues(){
		return Arrays.asList(Number.values());
	}
	
	@Override
	public String getName() {
		return "annotation_number";
	}

	@Override
	public Class<Annotation> getInputTypeClass() {
		return Annotation.class;
	}

	@Override
	public Class<Number> getReturnTypeClass() {
		return Number.class;
	}
	
}
