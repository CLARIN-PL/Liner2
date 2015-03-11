package g419.crete.api.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.features.enumvalues.Case;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingMentionCase extends AbstractClusterMentionFeature<Case>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestFollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		
		if(!closestFollowing.hasHead()) closestFollowing.assignHead();
		
		TokenAttributeIndex ai = closestFollowing.getSentence().getAttributeIndex();
		Token headToken = closestFollowing.getSentence().getTokens().get(closestFollowing.getHead());
		
		this.value = Case.fromValue(ai.getAttributeValue(headToken, "case"));
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_mention_case";
	}

	@Override
	public Class<Case> getReturnTypeClass() {
		return Case.class;
	}

}
