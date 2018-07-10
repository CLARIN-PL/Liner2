package g419.crete.core.cluster;

import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.crete.core.annotation.AbstractAnnotationSelector;

public abstract class AbstractAnnotationClusterer {
	public abstract AnnotationClusterSet resolveRelations(Document document, AbstractAnnotationSelector selector);
}
