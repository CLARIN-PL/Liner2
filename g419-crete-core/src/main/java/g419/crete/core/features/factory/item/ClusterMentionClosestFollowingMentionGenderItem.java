package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clustermention.following.ClusterMentionClosestFollowingMentionGender;
import g419.crete.core.features.enumvalues.Gender;
import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingMentionGenderItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Gender> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Gender> createFeature() {
		return new ClusterMentionClosestFollowingMentionGender();
	}

}
