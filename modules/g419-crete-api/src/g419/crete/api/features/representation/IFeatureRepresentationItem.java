package g419.crete.api.features.representation;

import g419.crete.api.classifier.AbstractCreteClassifier;

public interface IFeatureRepresentationItem<O> {
	public IFeatureRepresentation<O> createRepresentation(AbstractCreteClassifier<?, O, ?> classifier);
}
