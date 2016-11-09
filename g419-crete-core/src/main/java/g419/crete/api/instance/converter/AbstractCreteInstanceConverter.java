package g419.crete.api.instance.converter;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.representation.FeatureRepresentationFactory;
import g419.crete.api.features.representation.IFeatureRepresentation;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.structure.IHaveFeatures;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCreteInstanceConverter<T extends AbstractCreteInstance<?>, O> {
	
	AbstractCreteClassifier<?, O, ?> classifier;
	
	public AbstractCreteInstanceConverter(AbstractCreteClassifier<?, O, ?> cls){
		classifier = cls;
	}
	
	public List<O> convertInstances(List<T> instances){
		List<O> converted = new ArrayList<O>();
		for(T instance : instances)
			converted.add(convertSingleInstance(instance));
		return converted;
	}
	
	// ---------------------------- Abstract methods ----------------------
	public abstract O convertSingleInstance(T instance);
	public abstract Class<O> getRepresentationClass();
	
	protected O csi(T instance){
		FeatureRepresentationFactory fr = FeatureRepresentationFactory.getFactory();
		IFeatureRepresentation<O> repr = fr.getRepresentation(classifier);
		
		for(IHaveFeatures<?> instanceComponent : instance.getComponents())
			for(AbstractFeature<?, ?> feature : instanceComponent.extractFeatures())
				repr.addFeature(feature);
				
		return repr.get();
	}
	
}