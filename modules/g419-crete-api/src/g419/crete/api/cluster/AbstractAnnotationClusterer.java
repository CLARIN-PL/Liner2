package g419.crete.api.cluster;

import g419.corpus.structure.Document;
import g419.corpus.structure.AnnotationClusterSet;
import g419.crete.api.annotation.AbstractAnnotationSelector;

public abstract class AbstractAnnotationClusterer {
	public abstract AnnotationClusterSet resolveRelations(Document document, AbstractAnnotationSelector selector);
}
