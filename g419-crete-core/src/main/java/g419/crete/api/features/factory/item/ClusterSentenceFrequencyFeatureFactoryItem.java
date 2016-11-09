package g419.crete.api.features.factory.item;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clusters.ClusterFeatureSentenceFrequency;

public class ClusterSentenceFrequencyFeatureFactoryItem implements IFeatureFactoryItem<AnnotationCluster, Float> {

	@Override
	public AbstractFeature<AnnotationCluster, Float> createFeature() {
		return new ClusterFeatureSentenceFrequency();
	}

}
