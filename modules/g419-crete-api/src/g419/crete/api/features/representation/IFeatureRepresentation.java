package g419.crete.api.features.representation;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.features.AbstractFeature;

public abstract class IFeatureRepresentation<O> {
	public IFeatureRepresentation(AbstractCreteClassifier<?, O, ?> classifier){};
	
	protected O repr;
	public O get(){return repr;}
	
	@SuppressWarnings("unchecked")
	public void addFeature(AbstractFeature<?, ?> feature){
		
		if(feature.getReturnTypeClass().equals(Boolean.class)) addBooleanFeature((AbstractFeature<?, Boolean>) feature);
		else if(feature.getReturnTypeClass().equals(Integer.class)) addIntegerFeature((AbstractFeature<?, Integer>) feature);
		else if(feature.getReturnTypeClass().equals(Float.class)) addFloatFeature((AbstractFeature<?, Float>) feature);
		else if(Enum.class.isAssignableFrom(feature.getReturnTypeClass())) addEnumFeature((AbstractFeature<?, Enum<?>>) feature);
//		else addLexicalFeature((AbstractFeature<?, String>) feature);
	}
	
	public abstract void addBooleanFeature(AbstractFeature<?, Boolean> feature);
	public abstract void addIntegerFeature(AbstractFeature<?, Integer> feature);
	public abstract void addFloatFeature(AbstractFeature<?, Float> feature);
	public abstract void addEnumFeature(AbstractFeature<?, Enum<?>> feature);
	public abstract void addLexicalFeature(AbstractFeature<?, String> feature);
}