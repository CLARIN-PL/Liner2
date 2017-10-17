package g419.crete.core.features.factory.item;

import g419.crete.core.features.AbstractFeature;

public interface IFeatureFactoryItem<I, O> {
	public AbstractFeature<I,O> createFeature();
}
