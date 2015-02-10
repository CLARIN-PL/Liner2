package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.ClusterMentionClosestPreceedingMentionGender;
import g419.crete.api.features.enumvalues.Gender;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingMentionGenderItem   implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Gender> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Gender> createFeature() {
		return new ClusterMentionClosestPreceedingMentionGender();
	}

}
