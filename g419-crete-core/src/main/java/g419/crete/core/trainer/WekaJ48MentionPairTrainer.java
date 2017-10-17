package g419.crete.core.trainer;

import g419.crete.core.instance.MentionPairClassificationInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaJ48MentionPairTrainer extends AbstractCreteTrainer<Classifier, MentionPairClassificationInstance<Integer>, Instance, Integer>{

	@Override	public Class<Classifier> getModelClass() { return Classifier.class; }
	@Override public Class<MentionPairClassificationInstance<Integer>> getAbstractInstanceClass() {
		MentionPairClassificationInstance<Integer> inst = new MentionPairClassificationInstance<>();
		return (Class<MentionPairClassificationInstance<Integer>>) inst.getClass();
	}
	@Override 	public Class<Instance> getClassifierInstanceClass() { return Instance.class; }
	@Override	public Class<Integer> getLabelClass() { return Integer.class; }

}
