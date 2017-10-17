package g419.crete.core.features.annotations.pair;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSameEndWord extends AbstractAnnotationPairFeature<Boolean>{

	private boolean base = false;
	
	public AnnotationPairFeatureSameEndWord(boolean base){
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
		this.value = firstText.get(firstText.size() - 1).equalsIgnoreCase(secondText.get(secondText.size() - 1));
		
	}

	@Override
	public String getName() {
		return "annotationpair_same_endword" + (base ? "_base" : "");
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
