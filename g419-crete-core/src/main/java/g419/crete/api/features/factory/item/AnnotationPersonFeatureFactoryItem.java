package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.AnnotationFeaturePerson;
import g419.crete.api.features.enumvalues.Person;

public class AnnotationPersonFeatureFactoryItem implements IFeatureFactoryItem<Annotation, Person>{

	@Override
	public AbstractFeature<Annotation, Person> createFeature() {
		return new AnnotationFeaturePerson();
	}

}
