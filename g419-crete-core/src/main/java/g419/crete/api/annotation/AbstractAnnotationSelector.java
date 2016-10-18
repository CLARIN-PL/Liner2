package g419.crete.api.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for defining annotation selectors
 */
public abstract class AbstractAnnotationSelector {
	/**
	 * Returns all annotations in document that meet given criteria
	 *  Should be consistent with 'matches' i.e. all and only these annotations
	 *  in document for which matches(annotation) == true are returned
	 * @param document - input document for selecting annotations
	 * @return annotations that meet certain criteria
	 */
	public List<Annotation> selectAnnotations(Document document){
		List<Annotation> annotations = new ArrayList<>();
		for(Annotation annotation : document.getAnnotations())
			if(matches(annotation))
				annotations.add(annotation);

		return annotations;
	}


	/**
	 * Single annotation check for meeting given selector's criteria
	 * Should be consistent with 'selectAnnotations'
	 * @param annotation
	 * @return
	 */
	public abstract boolean matches(Annotation annotation);
}