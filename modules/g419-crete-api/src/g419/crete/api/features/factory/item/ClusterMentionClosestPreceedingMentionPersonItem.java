package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.preceeding.ClusterMentionClosestPreceedingMentionPerson;
import g419.crete.api.features.enumvalues.Person;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingMentionPersonItem   implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Person> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Person> createFeature() {
		return new ClusterMentionClosestPreceedingMentionPerson();
	}

}
