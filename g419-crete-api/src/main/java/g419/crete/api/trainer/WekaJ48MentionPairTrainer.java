package g419.crete.api.trainer;

import g419.crete.api.instance.MentionPairClassificationInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaJ48MentionPairTrainer extends AbstractCreteTrainer<Classifier, MentionPairClassificationInstance, Instance, Integer>{

	@Override	public Class<Classifier> getModelClass() { return Classifier.class; }
	@Override	public Class<MentionPairClassificationInstance> getAbstractInstanceClass() { return MentionPairClassificationInstance.class; }
	@Override 	public Class<Instance> getClassifierInstanceClass() { return Instance.class; }
	@Override	public Class<Integer> getLabelClass() { return Integer.class; }

}
