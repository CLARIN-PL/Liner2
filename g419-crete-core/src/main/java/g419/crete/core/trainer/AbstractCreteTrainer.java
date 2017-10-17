package g419.crete.core.trainer;

import g419.corpus.structure.Document;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.classifier.AbstractCreteClassifier;
import g419.crete.core.classifier.serialization.Serializer;
import g419.crete.core.instance.AbstractCreteInstance;
import g419.crete.core.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.core.instance.generator.AbstractCreteInstanceGenerator;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCreteTrainer <M, T extends AbstractCreteInstance<L>, I, L>{

	AbstractCreteClassifier<M, I, L> classifier;
	AbstractCreteInstanceGenerator<T, L> generator;
	AbstractCreteInstanceConverter<T, I> converter;
	
	public void setUp(AbstractCreteClassifier<M, I, L> clas, AbstractCreteInstanceGenerator<T, L> gen, AbstractCreteInstanceConverter<T, I> conv){
		this.classifier = clas;
		this.generator = gen;
		this.converter = conv;
	}
	
	
	public void addDocumentTrainingInstances(Document document, AbstractAnnotationSelector selector, AbstractAnnotationSelector singletonSelector){
		List<T> abstractInstances = generator.generateInstances(document, selector, singletonSelector);
		List<I> classifierInstances = converter.convertInstances(abstractInstances);
		List<L> instanceLabels = new ArrayList<L>();
		for(AbstractCreteInstance<L> abstractInstance : abstractInstances)
			instanceLabels.add(abstractInstance.getLabel());
		classifier.addTrainingInstances(classifierInstances, instanceLabels);
	}
	
	public void train(){
		classifier.train();
	}
	
	public Serializer<M> getTrainedModel(){
		return classifier.getModel();
	}
	//-------------------------- Abstract methods -------------------------
		
	//-------------------------- Abstract Generic methods --------------
	public abstract Class<M> getModelClass();
	public abstract Class<T> getAbstractInstanceClass();
	public abstract Class<I> getClassifierInstanceClass();
	public abstract Class<L> getLabelClass();

}