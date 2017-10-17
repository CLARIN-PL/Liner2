package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeatureFollowingRelativePronounDistance;

public class AnnotationFollowingRelativePronounDistanceItem implements IFeatureFactoryItem<Annotation, Integer> {

	@Override
	public AbstractFeature<Annotation, Integer> createFeature() {
		return new AnnotationFeatureFollowingRelativePronounDistance();
	}

}
