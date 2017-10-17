package g419.crete.core.features.representation;

import g419.crete.core.classifier.AbstractCreteClassifier;
import weka.core.Instance;

public class WekaInstanceFeatureRepresentationItem implements IFeatureRepresentationItem<Instance> {

	@Override
	public IFeatureRepresentation<Instance> createRepresentation(AbstractCreteClassifier<?, Instance, ?> classifier) {
		return new WekaInstanceFeatureRepresentation(classifier);
	}

}
