package g419.crete.core.features.representation;

import g419.crete.core.classifier.AbstractCreteClassifier;

import java.util.HashMap;

import weka.core.Instance;


public class FeatureRepresentationFactory {
	private static class FactoryHolder {
        private static final FeatureRepresentationFactory FACTORY = new FeatureRepresentationFactory();
    }
	public static FeatureRepresentationFactory getFactory(){
		return FactoryHolder.FACTORY;
	}
	
	private HashMap<Class<?>, IFeatureRepresentationItem<?>> totalRepresentations;
	
	private FeatureRepresentationFactory(){
		totalRepresentations = new HashMap<Class<?>, IFeatureRepresentationItem<?>>();	
		totalRepresentations.put(Instance.class, new WekaInstanceFeatureRepresentationItem());
	}
	
	@SuppressWarnings("unchecked")
	public <I extends Object> IFeatureRepresentation<I> getRepresentation(AbstractCreteClassifier<?, I, ?> classifier){
		IFeatureRepresentationItem<I> representationItem = (IFeatureRepresentationItem<I>) totalRepresentations.get(classifier.getInstanceClass());
		return representationItem.createRepresentation(classifier);
	}	
}