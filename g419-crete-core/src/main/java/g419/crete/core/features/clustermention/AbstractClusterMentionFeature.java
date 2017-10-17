package g419.crete.core.features.clustermention;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractClusterMentionFeature<T> extends AbstractFeature<Pair<Annotation, AnnotationCluster>, T> {

	@Override
	@SuppressWarnings("unchecked")
	public Class<Pair<Annotation, AnnotationCluster>> getInputTypeClass() {
		Pair<Annotation, AnnotationCluster> pair = new ImmutablePair<Annotation, AnnotationCluster>(null, null);
		return (Class<Pair<Annotation, AnnotationCluster>>) pair.getClass();
	}

	
}
