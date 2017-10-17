package g419.crete.core.features.annotations.pair;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSameMiddleWords extends AbstractAnnotationPairFeature<Boolean>{

	private boolean base = false;
	
	public AnnotationPairFeatureSameMiddleWords(boolean base){
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
		
		
		if(firstText.size() != secondText.size()){
			this.value = false;
		}
		else{
			this.value = true;
			for(int i = 1; i < firstText.size() - 1; i++){
				if(!firstText.get(i).equalsIgnoreCase(secondText.get(i))){
					this.value = false;
					break;
				}
			}
		}
		
	}

	@Override
	public String getName() {
		return "annotationpair_same_middleword" + (base ? "_base" : "");
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
