package g419.crete.api.features.clusters;

import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;

public class ClusterFeatureDocumentId extends AbstractFeature<AnnotationCluster, Integer>{

	@Override
	public void generateFeature(AnnotationCluster input) {
		String name = input.getDocument().getName();
		String[] split = name.split("\\/");
		String fileName = split[split.length - 1];
		String noExt = fileName.substring(0, fileName.length() - 4);
		
		this.value = Integer.parseInt(noExt);
	}

	@Override
	public String getName() {
		return "cluster_document_id";
	}

	@Override
	public Class<AnnotationCluster> getInputTypeClass() {
		return AnnotationCluster.class;
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}
	
}
