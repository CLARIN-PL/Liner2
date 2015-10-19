package g419.crete.api.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureChannel extends AbstractAnnotationPairFeature<String>{

	private final boolean firstChannel;
	
	public AnnotationPairFeatureChannel(boolean first) {
		this.firstChannel = first;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation first = input.getLeft();
		Annotation second = input.getRight();
		
		this.value = this.firstChannel ? first.getType() : second.getType();
	}

	@Override
	public String getName() {
		return "annotationpair_channel" + (this.firstChannel ? "_first" : "_second");
	}

	@Override
	public Class<String> getReturnTypeClass() {
		return String.class;
	}

	

}
