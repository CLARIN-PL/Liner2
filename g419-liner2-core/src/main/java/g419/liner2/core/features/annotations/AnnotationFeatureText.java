package g419.liner2.core.features.annotations;

import g419.corpus.structure.Annotation;

/**
 * Annotation feature generator that returns annotation text form.
 * @author czuk
 *
 */
public class AnnotationFeatureText extends AnnotationAtomicFeature {

	@Override
	public String generate(Annotation an) {
		return an.getText();
	}

}
