package g419.crete.api.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.pair.AnnotationPairFeatureChannel;
import g419.crete.api.features.factory.item.IFeatureFactoryItem;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureChannelItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, String> {

	private final boolean first;
	public AnnotationPairFeatureChannelItem(boolean first) {
		this.first = first;
	}
	
	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, String> createFeature() {
		return new AnnotationPairFeatureChannel(this.first);
	}

}
