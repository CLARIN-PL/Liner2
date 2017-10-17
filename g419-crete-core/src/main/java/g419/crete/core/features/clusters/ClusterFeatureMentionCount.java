package g419.crete.core.features.clusters;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;

public class ClusterFeatureMentionCount extends AbstractFeature<AnnotationCluster, Integer>{

	private Integer value;
	
	@Override
	public void generateFeature(AnnotationCluster input) {
		value = input.getAnnotations().size();
	}

	@Override
	public Integer getValue() { return value;	}

	@Override
	public Class<AnnotationCluster> getInputTypeClass() {return AnnotationCluster.class;}

	@Override
	public Class<Integer> getReturnTypeClass() { return Integer.class; }

	@Override
	public String getName() {
		return "cluster_mention_count";
	}

}
