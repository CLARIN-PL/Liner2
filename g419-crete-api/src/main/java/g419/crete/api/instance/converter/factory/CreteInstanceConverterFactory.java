package g419.crete.api.instance.converter.factory;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.converter.factory.item.ICreteInstanceConverterFactoryItem;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

public class CreteInstanceConverterFactory {

	private  HashMap<Triple<Class<?>, Class<?>, String>, ICreteInstanceConverterFactoryItem<?, ?>> instances;
	
	private static class FactoryHolder {
        private static final CreteInstanceConverterFactory FACTORY = new CreteInstanceConverterFactory();
    }

	public static CreteInstanceConverterFactory getFactory(){return FactoryHolder.FACTORY;}
	
	private CreteInstanceConverterFactory() {
		instances = new HashMap<Triple<Class<?>,Class<?>,String>, ICreteInstanceConverterFactoryItem<?,?>>(); 
	}
	
	@SuppressWarnings("unchecked")
	public  <T extends AbstractCreteInstance<?>, I extends Object> AbstractCreteInstanceConverter<T, I> getInstance(Class<T> abstractInstanceClass, Class<I> classifierInstanceClass, String name, AbstractCreteClassifier<?, I, ?> classifier){
		Triple<Class<T>, Class<I>, String> key = new ImmutableTriple<Class<T>, Class<I>, String>(abstractInstanceClass, classifierInstanceClass, name);
		return ((ICreteInstanceConverterFactoryItem<T, I>) instances.get(key)).createConverter(classifier);
	}
	
	
	public <T extends AbstractCreteInstance<?>, I extends Object> void registerInstance(Class<T> abstractInstanceClass, Class<I> classifierInstanceClass, String name, ICreteInstanceConverterFactoryItem<T, I> converter){
		Triple<Class<?>, Class<?>, String> key = new ImmutableTriple<Class<?>, Class<?>, String>(abstractInstanceClass, classifierInstanceClass, name);
		instances.put(key, converter);
	}
}
