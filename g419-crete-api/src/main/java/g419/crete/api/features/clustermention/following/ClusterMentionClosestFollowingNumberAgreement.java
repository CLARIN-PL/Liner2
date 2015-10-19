package g419.crete.api.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.annotations.AnnotationFeatureGender;
import g419.crete.api.features.annotations.AnnotationFeatureNumber;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.features.enumvalues.Gender;
import g419.crete.api.features.enumvalues.Number;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingNumberAgreement extends AbstractClusterMentionFeature<Boolean> {

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestfollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		if(closestfollowing == null){
			this.value = false;
			return;
		}
		
		AnnotationFeatureNumber numberMention = new AnnotationFeatureNumber();
		AnnotationFeatureNumber numberPreceding = new AnnotationFeatureNumber();
		
		numberMention.generateFeature(mention);
		numberPreceding.generateFeature(closestfollowing);
		
		if(Number.UNDEFINED.equals(numberMention.getValue())){
			this.value = false;
			return;
		}
		
		this.value = numberMention.getValue().equals(numberPreceding.getValue());
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_number_agreement";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}
	
}
