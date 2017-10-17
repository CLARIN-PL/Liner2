package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureFirstLength extends	AbstractAnnotationPairFeature<Integer> {

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		this.value = input.getLeft().getText().split(" ").length;
	}

	@Override
	public String getName() {
		return "annotationpair_first_length";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
