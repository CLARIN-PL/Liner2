package g419.crete.core.features.factory.item;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clusters.ClusterFeatureTermFrequency;

public class ClusterTermFrequencyFeatureFactoryItem implements IFeatureFactoryItem<AnnotationCluster, Float> {

	@Override
	public AbstractFeature<AnnotationCluster, Float> createFeature() {
		return new ClusterFeatureTermFrequency();
	}

}
