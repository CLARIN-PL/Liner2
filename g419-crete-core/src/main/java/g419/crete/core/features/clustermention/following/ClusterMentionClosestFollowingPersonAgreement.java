package g419.crete.core.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.annotations.AnnotationFeaturePerson;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.features.enumvalues.Person;
import g419.crete.core.structure.AnnotationUtil;
import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingPersonAgreement extends AbstractClusterMentionFeature<Boolean> {

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestfollowing = AnnotationUtil.getClosestFollowing(mention, cluster);
		if(closestfollowing == null){
			this.value = false;
			return;
		}
		
		AnnotationFeaturePerson personMention = new AnnotationFeaturePerson();
		AnnotationFeaturePerson personFollowing = new AnnotationFeaturePerson();
		
		personMention.generateFeature(mention);
		personFollowing.generateFeature(closestfollowing);
		
		if(Person.UNDEFINED.equals(personMention.getValue())){
			this.value = false;
			return;
		}
		
		this.value = personMention.getValue().equals(personFollowing.getValue());
	}

	@Override
	public String getName() {
		return "clustermention_closest_following_person_agreement";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}
	
}