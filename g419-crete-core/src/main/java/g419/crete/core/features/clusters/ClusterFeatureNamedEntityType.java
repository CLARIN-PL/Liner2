package g419.crete.core.features.clusters;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.enumvalues.NamedEntityType;

public class ClusterFeatureNamedEntityType extends AbstractFeature<AnnotationCluster, NamedEntityType>{

	@Override
	public void generateFeature(AnnotationCluster input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "cluster_ne_type";
	}

	@Override
	public Class<AnnotationCluster> getInputTypeClass() {
		return AnnotationCluster.class;
	}

	@Override
	public Class<NamedEntityType> getReturnTypeClass() {
		return NamedEntityType.class;
	}

}
