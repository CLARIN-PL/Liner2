package g419.crete.api.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.List;

public class AllAnnotationSelector extends AbstractAnnotationSelector{

	@Override
	public List<Annotation> selectAnnotations(Document document) {
		return document.getAnnotations();
	}

}
