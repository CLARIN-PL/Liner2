package g419.crete.api.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingFollowedBySubordConj extends AbstractClusterMentionFeature<Boolean>{

	public static final Set<String> subordinateConjunctions = new HashSet<String>(Arrays.asList("aby", "ażeby", "aniżeli",
		    "bo", "bowiem", "by", "byle",
		    "choć", "chociaż", "choćby", "czy", "czego",
		    "dlatego że", "dopóki",
		    "iżby", "ilekroć",
		    "jak", "jak gdyby", "jakby", "jako że", "jeżeli", "jeśli", "jakkolwiek",
		    "gdy", "gdyby", "gdyż",
		    "kiedy", "który", "którego", "które", "która", "którą",
		    "mimo że",
		    "niż", "niżeli", "niźli",
		    "odkąd",
		    "ponieważ", "podczas gdy", "pomimo że",
		    "skoro",
		    "że", "żeby"));
	
	public final int lookupDistance;
	
	public ClusterMentionClosestFollowingFollowedBySubordConj(int lookup) {
		this.lookupDistance = lookup;
	}
	
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
		
		TokenAttributeIndex ai  = closestFollowing.getSentence().getAttributeIndex();
		ArrayList<Token> tokens = closestFollowing.getSentence().getTokens();
		
		int inputIndex = closestFollowing.getEnd();
		int searchEnd = Math.min(inputIndex + lookupDistance, closestFollowing.getSentence().getTokens().size());
		
		for(int i = inputIndex; i < searchEnd; i++)
			if(subordinateConjunctions.contains(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_followed_by_subordinate_conj"+lookupDistance;
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
