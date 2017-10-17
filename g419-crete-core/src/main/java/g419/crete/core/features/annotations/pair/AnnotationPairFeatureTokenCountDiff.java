package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureTokenCountDiff extends  AbstractAnnotationPairFeature<Integer>{

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
				
		this.value = Math.abs(firstAnnotation.getTokens().size() - secondAnnotation.getTokens().size());
	}

	@Override
	public String getName() {
		return "annotationpair_token_count_diff";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
