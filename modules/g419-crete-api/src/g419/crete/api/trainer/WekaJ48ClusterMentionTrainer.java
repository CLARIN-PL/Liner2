package g419.crete.api.trainer;

import g419.crete.api.instance.ClusterClassificationInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaJ48ClusterMentionTrainer extends AbstractCreteTrainer<Classifier, ClusterClassificationInstance, Instance, Integer>{

	@Override public Class<Classifier> getModelClass() {return Classifier.class;}
	@Override public Class<ClusterClassificationInstance> getAbstractInstanceClass() {return ClusterClassificationInstance.class;}
	@Override public Class<Instance> getClassifierInstanceClass() {return Instance.class;}
	@Override public Class<Integer> getLabelClass() {	return Integer.class;}
}
