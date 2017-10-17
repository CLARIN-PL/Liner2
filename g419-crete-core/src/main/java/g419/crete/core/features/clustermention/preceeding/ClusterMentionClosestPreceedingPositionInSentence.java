package g419.crete.core.features.clustermention.preceeding;

import org.apache.commons.lang3.tuple.Pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.structure.AnnotationUtil;

public class ClusterMentionClosestPreceedingPositionInSentence extends AbstractClusterMentionFeature<Integer>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		
		if(closestPreceeding != null)
			this.value = closestPreceeding.getBegin();
		else
			this.value = -1;
		
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_position_in_sentence";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
