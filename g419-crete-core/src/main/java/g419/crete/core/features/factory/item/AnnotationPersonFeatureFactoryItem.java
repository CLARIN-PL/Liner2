package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeaturePerson;
import g419.crete.core.features.enumvalues.Person;

public class AnnotationPersonFeatureFactoryItem implements IFeatureFactoryItem<Annotation, Person>{

	@Override
	public AbstractFeature<Annotation, Person> createFeature() {
		return new AnnotationFeaturePerson();
	}

}
