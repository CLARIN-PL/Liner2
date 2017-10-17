package g419.crete.core.features.representation;

import g419.crete.core.classifier.AbstractCreteClassifier;

public interface IFeatureRepresentationItem<O> {
	public IFeatureRepresentation<O> createRepresentation(AbstractCreteClassifier<?, O, ?> classifier);
}
