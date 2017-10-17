package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureNumberAgreement extends  AbstractAnnotationPairFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		
		if(!firstAnnotation.hasHead()) firstAnnotation.assignHead();
		if(!secondAnnotation.hasHead()) secondAnnotation.assignHead();
		
		Token firstHead = firstAnnotation.getSentence().getTokens().get(firstAnnotation.getHead());
		String firstNumber = ai.getAttributeValue(firstHead, "number");
		
		Token secondHead = secondAnnotation.getSentence().getTokens().get(secondAnnotation.getHead());
		String secondNumber = ai.getAttributeValue(secondHead, "number");

		
		if(firstNumber == null){
			this.value = false;
		}
		else{
			this.value = firstNumber.equalsIgnoreCase(secondNumber);
		}
	}

	@Override
	public String getName() {
		return "annotationpair_number_agreement";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
