package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingIsReflexivePossesive extends AbstractClusterMentionFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		this.value = false;
		
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = false;
			return;
		}
		
		if(!closestPreceeding.hasHead()) closestPreceeding.assignHead();
		Token head = closestPreceeding.getSentence().getTokens().get(closestPreceeding.getHead());
		
		boolean siebie = "siebie".equalsIgnoreCase(head.getAttributeValue("base"));
		boolean swoj = "swój".equalsIgnoreCase(head.getAttributeValue("base"));
		
		this.value = siebie || swoj;
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_is_reflexive_possesive";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
