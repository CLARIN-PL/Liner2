package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeaturePreceedingRelativePronounCase;
import g419.crete.core.features.enumvalues.Case;

public class AnnotationPreceedingRelativePronounCaseItem implements IFeatureFactoryItem<Annotation, Case> {

	@Override
	public AbstractFeature<Annotation, Case> createFeature() {
		return new AnnotationFeaturePreceedingRelativePronounCase();
	}

}
