package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clustermention.following.ClusterMentionClosestFollowingPreceededBySubordConj;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingPreceededBySubordConjItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Boolean> {
//	static{
//		FeatureFactory.getFactory().register(new ClusterMentionClosestFollowingPreceededBySubordConjItem(2));
//	}


	private final int lookup;
	
	public ClusterMentionClosestFollowingPreceededBySubordConjItem(int lookup) {
		this.lookup = lookup;
	}
	
	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Boolean> createFeature() {
		return new ClusterMentionClosestFollowingPreceededBySubordConj(lookup);
	}

}
