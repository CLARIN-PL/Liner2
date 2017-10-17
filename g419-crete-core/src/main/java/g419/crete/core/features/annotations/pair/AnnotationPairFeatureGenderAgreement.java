package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureGenderAgreement extends  AbstractAnnotationPairFeature<Boolean>{

	private final boolean masculinumTolerance;
	
	public AnnotationPairFeatureGenderAgreement(boolean masculinumTolerance) {
		this.masculinumTolerance = masculinumTolerance;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		
		if(!firstAnnotation.hasHead()) firstAnnotation.assignHead();
		if(!secondAnnotation.hasHead()) secondAnnotation.assignHead();
		
		Token firstHead = firstAnnotation.getSentence().getTokens().get(firstAnnotation.getHead());
		String firstGender = ai.getAttributeValue(firstHead, "gender");
		
		Token secondHead = secondAnnotation.getSentence().getTokens().get(secondAnnotation.getHead());
		String secondGender = ai.getAttributeValue(secondHead, "gender");
		
		if(firstGender == null || secondGender == null){
			this.value = false;
		}
		else{
			this.value = this.masculinumTolerance ? firstGender.startsWith("m") && secondGender.startsWith("m") : firstGender.equalsIgnoreCase(secondGender);
		}
	}

	@Override
	public String getName() {
		return "annotationpair_gender_agreement" +( this.masculinumTolerance ? "_masc_tolerant" : "");
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
