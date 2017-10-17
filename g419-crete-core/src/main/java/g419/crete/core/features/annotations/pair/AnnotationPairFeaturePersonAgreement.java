package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeaturePersonAgreement extends  AbstractAnnotationPairFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		
		if(!firstAnnotation.hasHead()) firstAnnotation.assignHead();
		if(!secondAnnotation.hasHead()) secondAnnotation.assignHead();
		
		Token firstHead = firstAnnotation.getSentence().getTokens().get(firstAnnotation.getHead());
		String firstPerson = ai.getAttributeValue(firstHead, "person");
		
		Token secondHead = secondAnnotation.getSentence().getTokens().get(secondAnnotation.getHead());
		String secondPerson = ai.getAttributeValue(secondHead, "person");
		
		if(firstPerson == null){
			this.value = false;
		}
		else{
			this.value = firstPerson.equalsIgnoreCase(secondPerson); 
		}
	}

	@Override
	public String getName() {
		return "annotationpair_person_agreement";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
