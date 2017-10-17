package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeatureNumber;
import g419.crete.core.features.enumvalues.Number;

public class AnnotationNumberFeatureFactoryItem implements IFeatureFactoryItem<Annotation, Number>{

	@Override
	public AbstractFeature<Annotation, Number> createFeature() {
		return new AnnotationFeatureNumber();
	}

}
