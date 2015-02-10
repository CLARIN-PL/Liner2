package g419.crete.api.classifier.factory;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.factory.item.IClassifierFactoryItem;

import java.util.HashMap;
import java.util.List;



public class ClassifierFactory {

	private static class FactoryHolder {
        private static final ClassifierFactory FACTORY = new ClassifierFactory();
    }

	private HashMap<String, IClassifierFactoryItem<?, ?, ?>> instances;
	
	private ClassifierFactory(){
		instances = new HashMap<String, IClassifierFactoryItem<?,?,?>>();
	}
	
	public static ClassifierFactory getFactory(){return FactoryHolder.FACTORY;}
	
	public <M extends Object, I extends Object, L extends Object> void register(String name, IClassifierFactoryItem<M, I, L> classifierItem){
		instances.put(name, classifierItem);
	}
	
	@SuppressWarnings("unchecked")
	public <M extends Object, I extends Object, L extends Object> AbstractCreteClassifier<M, I, L> getClassifier(String name){
		return (AbstractCreteClassifier<M, I, L>) instances.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <M extends Object, I extends Object, L extends Object> AbstractCreteClassifier<M, I, L> getClassifier(Class<M> modelClass, Class<I> instanceClass, Class<L> labelClass, String name, List<String> features){
		AbstractCreteClassifier<M, I, L> classifier = (AbstractCreteClassifier<M, I, L>) instances.get(name).createClassifier(features);
		return classifier;
	}
	
}
