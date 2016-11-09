package g419.crete.api.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSameHeadBase extends  AbstractAnnotationPairFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		
		if(!firstAnnotation.hasHead()) firstAnnotation.assignHead();
		if(!secondAnnotation.hasHead()) secondAnnotation.assignHead();
		
		Token firstHead = firstAnnotation.getSentence().getTokens().get(firstAnnotation.getHead());
		String firstBase = ai.getAttributeValue(firstHead, "base");
		
		Token secondHead = secondAnnotation.getSentence().getTokens().get(secondAnnotation.getHead());
		String secondBase = ai.getAttributeValue(secondHead, "base");
		
		this.value = firstBase.equalsIgnoreCase(secondBase); 
	}

	@Override
	public String getName() {
		return "annotationpair_same_head_base";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
