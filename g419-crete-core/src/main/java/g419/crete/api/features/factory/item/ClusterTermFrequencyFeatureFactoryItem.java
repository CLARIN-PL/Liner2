package g419.crete.api.features.factory.item;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clusters.ClusterFeatureTermFrequency;

public class ClusterTermFrequencyFeatureFactoryItem implements IFeatureFactoryItem<AnnotationCluster, Float> {

	@Override
	public AbstractFeature<AnnotationCluster, Float> createFeature() {
		return new ClusterFeatureTermFrequency();
	}

}
