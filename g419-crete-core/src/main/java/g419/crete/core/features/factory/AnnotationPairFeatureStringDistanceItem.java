package g419.crete.core.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.pair.AnnotationPairFeatureStringDistance;
import g419.crete.core.features.factory.item.IFeatureFactoryItem;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureStringDistanceItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Float> {

	private final StringDistance measure;
	private final String name;
	private final boolean base;
	
	public AnnotationPairFeatureStringDistanceItem(StringDistance measure, String name, boolean base) {
		this.measure = measure;
		this.name = name;
		this.base = base;
	}
	
	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Float> createFeature() {
		return new AnnotationPairFeatureStringDistance(measure, name, base);
	}

}
