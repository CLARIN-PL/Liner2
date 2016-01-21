package g419.crete.api.features.representation;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaClassifier;
import g419.crete.api.classifier.WekaDecisionTreesClassifier;
import g419.crete.api.features.AbstractFeature;
import weka.core.Attribute;
import weka.core.Instance;

import java.util.List;


public class WekaInstanceFeatureRepresentation extends IFeatureRepresentation<Instance>{

	WekaClassifier classifier;
	
	public WekaInstanceFeatureRepresentation(AbstractCreteClassifier<?, Instance, ?> classifier) {
		super(classifier);
		this.classifier = ((WekaClassifier)classifier);
		repr = new Instance(this.classifier.getAttributes().size());
	}

	private int getAttributeIndex(AbstractFeature<?, ?> feature){
		return this.classifier.getAttributeIndex(feature.getName());
	}
	
	private Attribute getAttribute(AbstractFeature<?, ?> feature){
		List<Attribute> attributeList = this.classifier.getAttributes();
		return attributeList.get(this.classifier.getAttributeIndex(feature.getName()));
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
	public void addEnumFeature(AbstractFeature<?, Enum<?>> feature) {
		repr.setValue(getAttribute(feature), feature.getValue().toString());
	}

	@Override
	public void addLexicalFeature(AbstractFeature<?, String> feature) {
		repr.setValue(getAttribute(feature), feature.getValue());
	}
	
}