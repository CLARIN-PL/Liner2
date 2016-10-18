package g419.crete.api.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.pair.AnnotationPairFeatureSameEndWord;
import g419.crete.api.features.factory.item.IFeatureFactoryItem;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSameEndWordItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Boolean>{

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Boolean> createFeature() {
		return new AnnotationPairFeatureSameEndWord(false);
	}

	
}
