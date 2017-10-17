package g419.crete.core.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.pair.AnnotationPairFeatureText;
import g419.crete.core.features.factory.item.IFeatureFactoryItem;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureTextItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, String> {

		@Override
	public AbstractFeature<Pair<Annotation, Annotation>, String> createFeature() {
			return new AnnotationPairFeatureText();
	}

}
