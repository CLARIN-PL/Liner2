package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.annotations.AnnotationFeaturePerson;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.features.enumvalues.Person;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingPersonAgreement extends AbstractClusterMentionFeature<Boolean> {

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = false;
			return;
		}
		
		AnnotationFeaturePerson personMention = new AnnotationFeaturePerson();
		AnnotationFeaturePerson personPreceeding = new AnnotationFeaturePerson();
		
		personMention.generateFeature(mention);
		personPreceeding.generateFeature(closestPreceeding);
		
		if(Person.UNDEFINED.equals(personMention.getValue())){
			this.value = false;
			return;
		}
		
		this.value = personMention.getValue().equals(personPreceeding.getValue());
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_person_agreement";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}
	
}