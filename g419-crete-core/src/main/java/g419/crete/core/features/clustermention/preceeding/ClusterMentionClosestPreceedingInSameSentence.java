package g419.crete.core.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingInSameSentence extends AbstractClusterMentionFeature<Boolean>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null)
			this.value = false;
		else
			this.value = mention.getSentence().getOrd() == closestPreceeding.getSentence().getOrd();
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_in_same_sentence";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
