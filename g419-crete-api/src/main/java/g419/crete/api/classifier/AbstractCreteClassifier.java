package g419.crete.api.classifier;

import g419.crete.api.classifier.serialization.Serializer;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractCreteClassifier<M, I, L> {
	
	protected List<I> trainingInstances;
	protected List<L> trainingInstanceLabels;
	
	
	public AbstractCreteClassifier(){
		this.trainingInstances = new ArrayList<I>();
		this.trainingInstanceLabels = new ArrayList<L>();
	}
	
	protected Serializer<M> model;
	public void setModel(Serializer<M> model){
		this.model = model;
	}
	public Serializer<M> getModel(){
		return this.model;
	}
	
	public void addTrainingInstances(List<I> instances, List<L> labels){
		trainingInstances.addAll(instances);
		trainingInstanceLabels.addAll(labels);
	}
	
	// Trenowanie i klasyfikacja
	public abstract List<L> classify(List<I> instances);
	public abstract void train();
	public abstract Class<I> getInstanceClass();
	public abstract Class<L> getLabelClass();
	
}
