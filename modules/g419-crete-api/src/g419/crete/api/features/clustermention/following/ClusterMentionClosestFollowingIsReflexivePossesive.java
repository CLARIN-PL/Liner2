package g419.crete.api.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingIsReflexivePossesive extends AbstractClusterMentionFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		this.value = false;
		
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestFollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		if(closestFollowing == null){
			this.value = false;
			return;
		}
		
		if(!closestFollowing.hasHead()) closestFollowing.assignHead();
		Token head = closestFollowing.getSentence().getTokens().get(closestFollowing.getHead());
		
		boolean siebie = "siebie".equalsIgnoreCase(head.getAttributeValue("base"));
		boolean swoj = "swój".equalsIgnoreCase(head.getAttributeValue("base"));
		
		this.value = siebie || swoj;
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_is_reflexive_possesive";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
