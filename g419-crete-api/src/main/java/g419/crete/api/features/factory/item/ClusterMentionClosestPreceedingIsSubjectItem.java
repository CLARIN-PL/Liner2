package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.CreteOptions;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.preceeding.ClusterMentionClosestPreceedingIsSubject;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingIsSubjectItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Boolean>{

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Boolean> createFeature() {
		String modelPath = CreteOptions.getOptions().getProperties().getProperty("malt_path"); //TODO: fixme
		return new ClusterMentionClosestPreceedingIsSubject(modelPath);
	}

}
