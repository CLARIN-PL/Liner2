package g419.crete.api.trainer.factory;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.factory.ClassifierFactory;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.generator.AbstractCreteInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.api.trainer.AbstractCreteTrainer;

import java.util.HashMap;
import java.util.List;

public class CreteTrainerFactory {
private HashMap<String, CreteTrainerFactoryItem<?>> instances;
	
	private static class FactoryHolder {
        private static final CreteTrainerFactory FACTORY = new CreteTrainerFactory();
    }
	
	private CreteTrainerFactory(){
		instances = new HashMap<String, CreteTrainerFactoryItem<?>>();
	}
	
	public static CreteTrainerFactory getFactory(){return FactoryHolder.FACTORY;}
	
	public void register(String name, CreteTrainerFactoryItem<?> trainerItem){
		instances.put(name, trainerItem);
	}

	public <L extends Object> AbstractCreteTrainer<?, ? extends AbstractCreteInstance<L>, ?, L> getTrainer(String name, String classifierName, String generatorName, String converterName, List<String> features){
		return getInstance(name, classifierName, generatorName, converterName, features);
	}
	
	@SuppressWarnings("unchecked")
	private <M extends Object, T extends AbstractCreteInstance<L>, I extends Object, L extends Object> AbstractCreteTrainer<M, T, I, L> getInstance(String name, String classifierName, String generatorName, String converterName, List<String> features){
		AbstractCreteTrainer<M, T, I, L> trainer = (AbstractCreteTrainer<M, T, I, L>) ((CreteTrainerFactoryItem<L>)instances.get(name)).getTrainer();
		if(trainer == null) return null;
		
		AbstractCreteClassifier<M, I, L> clas =  ClassifierFactory.getFactory().getClassifier(trainer.getModelClass(), trainer.getClassifierInstanceClass(), trainer.getLabelClass(), classifierName, features);
		AbstractCreteInstanceGenerator<T, L> gen = CreteInstanceGeneratorFactory.getFactory().getInstance(trainer.getAbstractInstanceClass(), trainer.getLabelClass(), generatorName, features);
		AbstractCreteInstanceConverter<T, I> conv = CreteInstanceConverterFactory.getFactory().getInstance(trainer.getAbstractInstanceClass(), trainer.getClassifierInstanceClass(), converterName, clas);
		
		trainer.setUp(clas, gen, conv);
		
		return trainer;
	}
}
