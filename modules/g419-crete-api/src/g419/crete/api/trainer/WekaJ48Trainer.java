package g419.crete.api.trainer;

import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.core.Instance;
import g419.crete.api.instance.ClusterClassificationInstance;

public class WekaJ48Trainer extends AbstractCreteTrainer<ClassifierSplitModel, ClusterClassificationInstance, Instance, Integer>{

	@Override public Class<ClassifierSplitModel> getModelClass() {return ClassifierSplitModel.class;}
	@Override public Class<ClusterClassificationInstance> getAbstractInstanceClass() {return ClusterClassificationInstance.class;}
	@Override public Class<Instance> getClassifierInstanceClass() {return Instance.class;}
	@Override public Class<Integer> getLabelClass() {	return Integer.class;}
}
