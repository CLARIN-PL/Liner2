package g419.crete.core.trainer;

import g419.crete.core.instance.ClusterClassificationInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaJ48ClusterMentionTrainer extends AbstractCreteTrainer<Classifier, ClusterClassificationInstance, Instance, Double>{

	@Override public Class<Classifier> getModelClass() {return Classifier.class;}
	@Override public Class<ClusterClassificationInstance> getAbstractInstanceClass() {return ClusterClassificationInstance.class;}
	@Override public Class<Instance> getClassifierInstanceClass() {return Instance.class;}
	@Override public Class<Double> getLabelClass() {	return Double.class;}
}
