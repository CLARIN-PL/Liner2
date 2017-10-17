package g419.crete.core.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.pair.AnnotationPairFeatureSecondLength;
import g419.crete.core.features.factory.item.IFeatureFactoryItem;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSecondLengthItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Integer>  {

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Integer> createFeature() {
		return new AnnotationPairFeatureSecondLength();
	}

}
