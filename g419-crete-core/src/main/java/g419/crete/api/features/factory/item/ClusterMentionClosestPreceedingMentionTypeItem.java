package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.preceeding.ClusterMentionClosestPreceedingMentionType;
import g419.crete.api.features.enumvalues.MentionType;

import org.apache.commons.lang3.tuple.Pair;


public class ClusterMentionClosestPreceedingMentionTypeItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, MentionType> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, MentionType> createFeature() {
		return new ClusterMentionClosestPreceedingMentionType();
	}

}
