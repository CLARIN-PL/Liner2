package g419.crete.api.features.factory.item;

import org.apache.commons.lang3.tuple.Pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.following.ClusterMentionClosestFollowingMentionNumber;
import g419.crete.api.features.enumvalues.Number;

public class ClusterMentionClosestFollowingMentionNumberItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Number> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Number> createFeature() {
		return new ClusterMentionClosestFollowingMentionNumber();
	}

}
