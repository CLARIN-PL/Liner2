package g419.crete.api.structure;

import java.util.ArrayList;
import java.util.List;

import g419.crete.api.features.AbstractFeature;

public abstract class IHaveFeatures<T> {
	protected T featureHolder;
	protected List<AbstractFeature<T, ?>> features;
	protected boolean featureValuesUpToDate = false;
	
	public T getHolder(){
		return featureHolder;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getHolderClass(){
		return (Class<T>) featureHolder.getClass();
	}
	
	public List<AbstractFeature<T, ?>> extractFeatures(){
		if(!featureValuesUpToDate) generateFeatures();
		return features;
	}
	
	public void generateFeatures(){
		for(AbstractFeature<T, ?> feature : features) feature.generateFeature(featureHolder);
		featureValuesUpToDate = true;
	}
	
	public void addFeature(AbstractFeature<T, ?> feature){
		features.add(feature);
	}
	
	public IHaveFeatures(T holder){
		featureHolder = holder;
		features = new ArrayList<AbstractFeature<T,?>>();
	}
	
}
