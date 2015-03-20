package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.AnnotationFeaturePreceedingSubordinateConjunction;

public class AnnotationPreceedingSubordinateConjunctionItem implements IFeatureFactoryItem<Annotation, Boolean> {

	@Override
	public AbstractFeature<Annotation, Boolean> createFeature() {
		return new AnnotationFeaturePreceedingSubordinateConjunction();
	}

}
