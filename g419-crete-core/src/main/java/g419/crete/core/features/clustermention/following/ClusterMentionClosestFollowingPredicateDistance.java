package g419.crete.core.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.structure.AnnotationUtil;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingPredicateDistance extends AbstractClusterMentionFeature<Integer> {

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestFollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		if(closestFollowing == null){
			this.value = 10000;
			return;
		}
			
		List<Token> tokensFollowing = AnnotationUtil.tokensBetweenAnnotations(mention, closestFollowing, cluster.getDocument());
		
		int count = 0;
		for(Token t: tokensFollowing) 
			if(t.getAttributeValue("pos") == "verb")
				count++;
		
		this.value = count;
		
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_predicate_distance";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
