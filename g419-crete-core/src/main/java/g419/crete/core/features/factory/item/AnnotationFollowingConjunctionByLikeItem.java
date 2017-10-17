package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.AnnotationFeatureFollowingConjunctionByLike;

public class AnnotationFollowingConjunctionByLikeItem implements IFeatureFactoryItem<Annotation, Boolean> {

	private final int lookup;
	
	public AnnotationFollowingConjunctionByLikeItem(int lookup) {
		this.lookup = lookup;
	}
	
	
	@Override
	public AbstractFeature<Annotation, Boolean> createFeature() {
		return new AnnotationFeatureFollowingConjunctionByLike(lookup);
	}

}
