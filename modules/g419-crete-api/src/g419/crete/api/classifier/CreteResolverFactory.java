package g419.crete.api.classifier;

import g419.crete.api.classifier.factory.ClassifierFactory;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.generator.AbstractCreteInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;

import java.util.HashMap;
import java.util.List;

public class CreteResolverFactory {
	
	private HashMap<String, AbstractCreteResolver<?,?,?,?>> instances;
	
	private static class FactoryHolder {
        private static final CreteResolverFactory FACTORY = new CreteResolverFactory();
    }
	
	private CreteResolverFactory(){
		instances = new HashMap<String, AbstractCreteResolver<?,?,?,?>>();
	}
	
	public static CreteResolverFactory getFactory(){return FactoryHolder.FACTORY;}
	
	public void register(String name, AbstractCreteResolver<?,?,?,?> resolver){
		instances.put(name, resolver);
	}

	@SuppressWarnings("unchecked")
	public <M extends Object, T extends AbstractCreteInstance<L>,I extends Object, L extends Object> AbstractCreteResolver<M, T, I, L> getInstance(String name, String classifierName, String generatorName, String converterName, List<String> featureNames){
		AbstractCreteResolver<M, T, I, L> resolver = (AbstractCreteResolver<M, T, I, L>) instances.get(name);
		if(resolver == null) return null;
		
		AbstractCreteClassifier<M, I, L> clas =  ClassifierFactory.getFactory().getClassifier(resolver.getModelClass(), resolver.getClassifierInstanceClass(), resolver.getLabelClass(), classifierName, featureNames);
		AbstractCreteInstanceGenerator<T, L> gen = CreteInstanceGeneratorFactory.getFactory().getInstance(resolver.getAbstractInstanceClass(), resolver.getLabelClass(), generatorName, featureNames);
		AbstractCreteInstanceConverter<T, I> conv = CreteInstanceConverterFactory.getFactory().getInstance(resolver.getAbstractInstanceClass(), resolver.getClassifierInstanceClass(), converterName, clas);
		
		resolver.setUp(clas, gen, conv);
		
		return resolver;
	}

}
