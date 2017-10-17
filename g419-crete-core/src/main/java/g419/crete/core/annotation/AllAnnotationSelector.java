package g419.crete.core.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.List;

public class AllAnnotationSelector extends AbstractAnnotationSelector{

	@Override public List<Annotation> selectAnnotations(Document document) {
		return document.getAnnotations();
	}
	@Override public boolean matches(Annotation annotation) {return true;}

}
