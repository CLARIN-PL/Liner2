package g419.crete.api.features.representation;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaDecisionTreesClassifier;
import g419.crete.api.features.AbstractFeature;
import weka.core.Attribute;
import weka.core.Instance;


public class WekaInstanceFeatureRepresentation extends IFeatureRepresentation<Instance>{

	WekaDecisionTreesClassifier classifier;
	
	public WekaInstanceFeatureRepresentation(AbstractCreteClassifier<?, Instance, ?> classifier) {
		super(classifier);
		this.classifier = ((WekaDecisionTreesClassifier)classifier);
		repr = new Instance(this.classifier.getAttributes().size());
	}

	private int getAttributeIndex(AbstractFeature<?, ?> feature){
		return this.classifier.getAttributeIndex(feature.getName());
	}
	
	private Attribute getAttribute(AbstractFeature<?, ?> feature){
		return this.classifier.getAttributes().get(this.classifier.getAttributeIndex(feature.getName()));
	}
	
	@Override
	public void addBooleanFeature(AbstractFeature<?, Boolean> feature) {
		repr.setValue(getAttributeIndex(feature), feature.getValue()?1.0:0.0);
	}

	@Override
	public void addIntegerFeature(AbstractFeature<?, Integer> feature) {
		repr.setValue(getAttributeIndex(feature), feature.getValue());
	}

	@Override
	public void addFloatFeature(AbstractFeature<?, Float> feature) {
		repr.setValue(getAttributeIndex(feature), feature.getValue());
	}

	@Override
	public void addEnumFeature(AbstractFeature<?, Enum> feature) {
		repr.setValue(getAttribute(feature), feature.getValue().toString());
	}
	
}