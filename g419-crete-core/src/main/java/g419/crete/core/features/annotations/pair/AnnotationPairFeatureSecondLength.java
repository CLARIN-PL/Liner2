package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSecondLength extends	AbstractAnnotationPairFeature<Integer> {

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		this.value = input.getRight().getText().split(" ").length;
	}

	@Override
	public String getName() {
		return "annotationpair_second_length";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
