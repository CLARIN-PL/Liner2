package g419.crete.api.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.List;

public abstract class AbstractAnnotationSelector {
	public abstract List<Annotation> selectAnnotations(Document document);
}
