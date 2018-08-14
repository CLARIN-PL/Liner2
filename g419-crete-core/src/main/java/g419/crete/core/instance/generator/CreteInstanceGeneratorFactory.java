package g419.crete.core.instance.generator;

import g419.crete.core.instance.AbstractCreteInstance;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;

public class CreteInstanceGeneratorFactory {
	
	private  HashMap<Triple<Class<?>, Class<?>, String>, AbstractCreteInstanceGenerator<?, ?>> instances;
	
	private static class FactoryHolder {
        private static final CreteInstanceGeneratorFactory FACTORY = new CreteInstanceGeneratorFactory();
    }

	public static CreteInstanceGeneratorFactory getFactory(){return FactoryHolder.FACTORY;}
	
	private CreteInstanceGeneratorFactory() {
		instances = new HashMap<Triple<Class<?>,Class<?>,String>, AbstractCreteInstanceGenerator<?,?>>();
	}
	
	@SuppressWarnings("unchecked")
	public  <T extends AbstractCreteInstance<L>, L extends Object> AbstractCreteInstanceGenerator<T, L> getInstance(Class<T> abstractInstanceClass, Class<L> labelClass, String name, List<String> features){
		Triple<Class<T>, Class<L>, String> key = new ImmutableTriple<Class<T>, Class<L>, String>(abstractInstanceClass, labelClass, name);
		AbstractCreteInstanceGenerator<T, L> generator = (AbstractCreteInstanceGenerator<T, L>) instances.get(key);
		generator.setFeatures(features);
		return generator;
	}
	
	
	public <T extends AbstractCreteInstance<L>, L extends Object> void registerInstance(Class<T> abstractInstanceClass, Class<L> labelClass, String name, AbstractCreteInstanceGenerator<T, L> generator){
		Triple<Class<?>, Class<?>, String> key = new ImmutableTriple<Class<?>, Class<?>, String>(abstractInstanceClass, labelClass, name);
		instances.put(key, generator);
	}
	
	
}
