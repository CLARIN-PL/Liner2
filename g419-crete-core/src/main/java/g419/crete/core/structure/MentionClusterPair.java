package g419.crete.core.structure;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;

import org.apache.commons.lang3.tuple.Pair;

public class MentionClusterPair extends IHaveFeatures<Pair<Annotation, AnnotationCluster>>{

	public MentionClusterPair(Pair<Annotation, AnnotationCluster> holder) {
		super(holder);
	}

}
