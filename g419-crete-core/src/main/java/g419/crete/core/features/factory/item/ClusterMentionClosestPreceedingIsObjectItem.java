package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.CreteOptions;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clustermention.preceeding.ClusterMentionClosestPreceedingIsObject;
import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingIsObjectItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Boolean>{

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Boolean> createFeature() {
		String modelPath = CreteOptions.getOptions().getProperties().getProperty("malt_path"); // TODO: fixme
		return new ClusterMentionClosestPreceedingIsObject(modelPath);
	}

}
