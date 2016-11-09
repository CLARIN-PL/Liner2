package g419.crete.api.features.factory.item;

import g419.crete.api.features.AbstractFeature;

public interface IFeatureFactoryItem<I, O> {
	public AbstractFeature<I,O> createFeature();
}
