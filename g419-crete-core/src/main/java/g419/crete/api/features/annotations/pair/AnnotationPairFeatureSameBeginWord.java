package g419.crete.api.features.annotations.pair;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSameBeginWord extends AbstractAnnotationPairFeature<Boolean>{

	private boolean base = false;
	
	public AnnotationPairFeatureSameBeginWord(boolean base){
		this.base = base;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		
		List<String> firstText, secondText;
		if(base){
			firstText = Arrays.asList(firstAnnotation.getBaseText().split(" "));
			secondText = Arrays.asList(secondAnnotation.getBaseText().split(" "));
		}
		else{
			firstText = Arrays.asList(firstAnnotation.getText().split(" "));
			secondText = Arrays.asList(secondAnnotation.getText().split(" "));
		}
		this.value = firstText.get(0).equalsIgnoreCase(secondText.get(0));
		
	}

	@Override
	public String getName() {
		return "annotationpair_same_beginword" + (base ? "_base" : "");
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
