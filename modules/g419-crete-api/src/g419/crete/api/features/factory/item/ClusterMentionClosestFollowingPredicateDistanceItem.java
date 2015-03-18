package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.following.ClusterMentionClosestFollowingPredicateDistance;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingPredicateDistanceItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Integer> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Integer> createFeature() {
		return new ClusterMentionClosestFollowingPredicateDistance();
	}

}
