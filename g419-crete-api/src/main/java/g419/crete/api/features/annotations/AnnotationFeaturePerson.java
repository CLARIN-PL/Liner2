package g419.crete.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.enumvalues.Person;

import java.util.Arrays;
import java.util.List;

public class AnnotationFeaturePerson extends AbstractFeature<Annotation, Person> {
	
	public static final String AGLT_CLASS = "aglt";
	public static final String QUB_CLASS = "qub";
	
	@Override
	public void generateFeature(Annotation input) {
		input.assignHead();
		Token headToken = input.getSentence().getTokens().get(input.getHead());
		TokenAttributeIndex ai = input.getSentence().getAttributeIndex();
		Sentence sentence = input.getSentence();
		int positionInSentence = input.getHead();
		
		// 1. verb + aglt
		if(positionInSentence + 1 < sentence.getTokens().size()){
			Token agltCandidate = sentence.getTokens().get(positionInSentence + 1);
			if(AGLT_CLASS.equalsIgnoreCase(ai.getAttributeValue(agltCandidate, "class"))){
				this.value = Person.fromValue(ai.getAttributeValue(agltCandidate, "person"));
				return;
			}
		}
		
		
		// 2. verb + qub + aglt
		if(positionInSentence + 2 < sentence.getTokens().size()){
			Token qubCandidate = sentence.getTokens().get(positionInSentence + 1);
			Token agltCandidate = sentence.getTokens().get(positionInSentence + 2);
			if(AGLT_CLASS.equalsIgnoreCase(ai.getAttributeValue(agltCandidate, "class")) && QUB_CLASS.equalsIgnoreCase(ai.getAttributeValue(qubCandidate, "class"))){
				this.value = Person.fromValue(ai.getAttributeValue(agltCandidate, "person"));
				return;
			}
		}
		
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
