package g419.crete.core.features.factory.item;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clusters.ClusterFeatureDocumentId;

public class ClusterDocumentIdFeatureFactoryItem implements IFeatureFactoryItem<AnnotationCluster, Integer> {

	@Override
	public AbstractFeature<AnnotationCluster, Integer> createFeature() {
		return new ClusterFeatureDocumentId();
	}


}
