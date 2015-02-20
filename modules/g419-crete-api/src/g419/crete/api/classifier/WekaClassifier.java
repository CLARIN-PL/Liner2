package g419.crete.api.classifier;

import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.factory.FeatureFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public abstract class WekaClassifier<T, L> extends AbstractCreteClassifier<T, Instance, L>{
	protected Instances instances;
	public Instances getInstancesCopy(){return new Instances(instances);}
	
	protected ArrayList<Attribute> attributes;
	public ArrayList<Attribute> getAttributes(){
		return attributes;
	}
	protected HashMap<String, Integer> attributesByNames;
	public Integer getAttributeIndex(String name){
		return attributesByNames.get(name);
	}
	
	public WekaClassifier(List<String> features) {
		attributes = new ArrayList<Attribute>();
		attributesByNames = new HashMap<String, Integer>();
		constructAttributes(features);
		
		FastVector fvWekaAttributes = new FastVector(attributes.size());
		for(Attribute attr : attributes) fvWekaAttributes.addElement(attr);
		instances = new Instances("Coref", fvWekaAttributes, trainingInstances.size());
	}
	
	private void constructAttributes(List<String> features){
		int attrIndex = 0;
		for(String featureName : features){
			AbstractFeature<?,?> feature = FeatureFactory.getFactory().getFeature(featureName);
			Attribute attr = constructAttribute(feature);
			attributes.add(attr);
			attributesByNames.put(feature.getName(), attrIndex++);
		}
		
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("NON_COREF");
		fvClassVal.addElement("COREF");
		Attribute classAttr = new Attribute("CLASS", fvClassVal);
		attributes.add(classAttr);
		attributesByNames.put("CLASS", attributes.size() - 1);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Attribute constructAttribute(AbstractFeature<?,?> feature){
		if(Enum.class.isAssignableFrom(feature.getReturnTypeClass())){
			FastVector fvEnumValues = new FastVector(feature.getSize());
			
			for(Object e : feature.getAllValues())
				fvEnumValues.addElement(e.toString());
			
			return new Attribute(feature.getName(), fvEnumValues);
		}
		else{
			return new Attribute(feature.getName());
		}
	}
	
	@Override public Class<Instance> getInstanceClass() { return Instance.class;}
}
