package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnotationPairFeatureCosineBaseSimilarity extends  AbstractAnnotationPairFeature<Float>{

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		
		List<String> firstText = Arrays.asList(firstAnnotation.getBaseText().split(" "));
		List<String> secondText = Arrays.asList(secondAnnotation.getBaseText().split(" "));
		
		Map<String, Long> firstVector = firstText.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
		Map<String, Long> secondVector = secondText.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
		
		double numerator = 0.0f;
		double denominator =  
				Math.sqrt(firstVector.values().stream().reduce(0l, (sum, val) -> sum += val * val)) 
				+ Math.sqrt(secondVector.values().stream().reduce(0l, (sum, val) -> sum += val * val));
		
		for(String key : firstVector.keySet())
			if(secondVector.containsKey(key))
				numerator += firstVector.get(key) * secondVector.get(key);
		
		this.value =new Float(numerator / denominator);
	}

	@Override
	public String getName() {
		return "annotationpair_cossimilarity_base";
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

}
