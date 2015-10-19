package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.pair.AnnotationPairFeatureSameChanName;
import g419.crete.api.features.annotations.pair.AnnotationPairFeatureTokenCountDiff;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureTokenCountDiffItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Integer> {

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Integer>  createFeature() {
		return new AnnotationPairFeatureTokenCountDiff();
	}

}
