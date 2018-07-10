package g419.crete.core.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.structure.AnnotationUtil;
import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingPositionInSentence extends AbstractClusterMentionFeature<Integer>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestFollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		
		if(closestFollowing != null)
			this.value = closestFollowing.getBegin();
		else
			this.value = -1;
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_position_in_sentence";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
