package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeatureGender;
import g419.crete.core.features.enumvalues.Gender;


public class AnnotationGenderFeatureFactoryItem implements IFeatureFactoryItem<Annotation, Gender> {

	@Override
	public AbstractFeature<Annotation, Gender> createFeature() {
		return new AnnotationFeatureGender();
	}
}
