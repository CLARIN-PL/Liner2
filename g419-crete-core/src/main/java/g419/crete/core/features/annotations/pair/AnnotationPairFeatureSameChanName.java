package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSameChanName extends  AbstractAnnotationPairFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
				
		this.value = firstAnnotation.getType().equals(secondAnnotation.getType());
	}

	@Override
	public String getName() {
		return "annotationpair_same_chan_name";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
