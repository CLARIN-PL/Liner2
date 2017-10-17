package g419.crete.core.features.factory.item;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clusters.ClusterFeatureSentenceFrequency;

public class ClusterSentenceFrequencyFeatureFactoryItem implements IFeatureFactoryItem<AnnotationCluster, Float> {

	@Override
	public AbstractFeature<AnnotationCluster, Float> createFeature() {
		return new ClusterFeatureSentenceFrequency();
	}

}
