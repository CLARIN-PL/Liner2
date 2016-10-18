package g419.crete.api.features.annotations.pair;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureIsAcronym extends AbstractAnnotationPairFeature<Boolean>{
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
	
		List<String> firstText = Arrays.asList(firstAnnotation.getText().split(" "));
		List<String> secondText = Arrays.asList(secondAnnotation.getText().split(" "));
		
		List<String> shorter, longer;
		
		if(firstText.size() < secondText.size()){
			shorter = firstText;
			longer = secondText;
		}
		else{
			shorter = secondText;
			longer = firstText;
		}
		
		if(shorter.size() > 1 || longer.size() <= 1){
			this.value = false;
		}
		else{
			String shorterText = shorter.get(0);
			String acroCapt = longer.stream().map((s) -> s.substring(0,1)).filter(s -> s.toUpperCase().equals(s)).reduce("", (s, c) -> s + c);
			String acroAll = longer.stream().map((s) -> s.substring(0,1)).reduce("", (s, c) -> s + c);
			this.value = shorterText.equalsIgnoreCase(acroAll) || shorterText.equalsIgnoreCase(acroCapt);
			if(this.value){
				System.out.println(shorter + " is acro of  " + longer);
			}
		}
	}

	@Override
	public String getName() {
		return "annotationpair_acronym";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
