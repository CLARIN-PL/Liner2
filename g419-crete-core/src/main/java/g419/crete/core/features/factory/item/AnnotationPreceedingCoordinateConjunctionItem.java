package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeaturePreceedingCoordinateConjunction;

public class AnnotationPreceedingCoordinateConjunctionItem implements IFeatureFactoryItem<Annotation, Boolean> {

	@Override
	public AbstractFeature<Annotation, Boolean> createFeature() {
		return new AnnotationFeaturePreceedingCoordinateConjunction();
	}

}
