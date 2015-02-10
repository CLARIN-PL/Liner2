package g419.crete.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.enumvalues.Person;

import java.util.Arrays;
import java.util.List;

public class AnnotationFeaturePerson extends AbstractFeature<Annotation, Person> {

	@Override
	public void generateFeature(Annotation input) {
		input.assignHead();
		Token headToken = input.getSentence().getTokens().get(input.getHead());
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		this.value = Person.fromValue(ai.getAttributeValue(headToken, "person"));
	}
	
	@Override
	public int getSize() {
		return Person.values().length;
	}

	@Override
	public List<Person> getAllValues(){
		return Arrays.asList(Person.values());
	}

	@Override
	public String getName() {
		return "annotation_person";
	}

	@Override
	public Class<Annotation> getInputTypeClass() {
		return Annotation.class;
	}

	@Override
	public Class<Person> getReturnTypeClass() {
		return Person.class;
	}
	
	
}
