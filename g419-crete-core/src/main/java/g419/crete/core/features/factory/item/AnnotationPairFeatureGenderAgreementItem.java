package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.pair.AnnotationPairFeatureGenderAgreement;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureGenderAgreementItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Boolean> {

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Boolean>  createFeature() {
		return new AnnotationPairFeatureGenderAgreement(false);
	}

}
