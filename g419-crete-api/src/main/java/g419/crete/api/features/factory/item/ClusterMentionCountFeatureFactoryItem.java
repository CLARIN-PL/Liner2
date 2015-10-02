package g419.crete.api.features.factory.item;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clusters.ClusterFeatureMentionCount;

public class ClusterMentionCountFeatureFactoryItem implements IFeatureFactoryItem<AnnotationCluster, Integer> {

	@Override
	public AbstractFeature<AnnotationCluster, Integer> createFeature() {
		return new ClusterFeatureMentionCount();
	}

}