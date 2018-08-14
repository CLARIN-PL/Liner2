package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.preceeding.ClusterMentionClosestPreceedingSentenceDistance;
import org.apache.commons.lang3.tuple.Pair;


public class ClusterMentionClosestPreceedingSentenceDistanceItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Integer>{

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Integer> createFeature() {
		return new ClusterMentionClosestPreceedingSentenceDistance();
	}

}
