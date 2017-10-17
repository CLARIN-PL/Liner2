package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.pair.AnnotationPairFeatureCosineBaseSimilarity;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureCosineBaseSimilarityItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Float> {

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Float>  createFeature() {
		return new AnnotationPairFeatureCosineBaseSimilarity();
	}

}
