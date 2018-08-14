package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clustermention.preceeding.ClusterMentionClosestPreceedingMentionType;
import g419.crete.core.features.enumvalues.MentionType;
import org.apache.commons.lang3.tuple.Pair;


public class ClusterMentionClosestPreceedingMentionTypeItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, MentionType> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, MentionType> createFeature() {
		return new ClusterMentionClosestPreceedingMentionType();
	}

}
