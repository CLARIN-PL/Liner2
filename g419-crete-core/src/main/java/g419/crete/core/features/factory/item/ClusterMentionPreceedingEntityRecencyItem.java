package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clustermention.preceeding.ClusterMentionPreceedingEntityRecency;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionPreceedingEntityRecencyItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Float>{

	private int sentences;
	
	public ClusterMentionPreceedingEntityRecencyItem(int sentences) {
		this.sentences = sentences;
	}
	
	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Float> createFeature() {
		return new ClusterMentionPreceedingEntityRecency(sentences);
	}

}
