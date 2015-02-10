package g419.crete.api.features.clustermention;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.enumvalues.Gender;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingMentionGender extends AbstractClusterMentionFeature<Gender>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestFollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		closestFollowing.assignHead();
		Token headToken = closestFollowing.getSentence().getTokens().get(closestFollowing.getHead());
		
		TokenAttributeIndex ai = closestFollowing.getSentence().getAttributeIndex();
				
		this.value = Gender.valueOf(ai.getAttributeValue(headToken, "gender"));
		
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_gender";
	}

	@Override
	public Class<Gender> getReturnTypeClass() {
		return Gender.class;
	}

	@Override
	public int getSize() {
		return Gender.values().length;
	}

	@Override
	public List<Gender> getAllValues(){
		return Arrays.asList(Gender.values());
	}
	
}
