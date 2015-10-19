package g419.crete.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.enumvalues.Gender;

import java.util.Arrays;
import java.util.List;

public class AnnotationFeatureGender extends AbstractFeature<Annotation, Gender> {

	@Override
	public void generateFeature(Annotation input) {
		input.assignHead();
		Token headToken = input.getSentence().getTokens().get(input.getHead());
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		String gender = ai.getAttributeValue(headToken, "gender");
		
		this.value = Gender.fromValue(gender);
	}

	@Override
	public Class<Annotation> getInputTypeClass() { return Annotation.class;}

	@Override
	public Class<Gender> getReturnTypeClass() {return Gender.class;}

	@Override
	public String getName() {
		return "annotation_gender";
	}

	@Override
	public int getSize() {
		return Gender.values().length;
	}

	@Override
	public List<Gender> getAllValues(){
		return Arrays.asList(Gender.values());
	}
}
