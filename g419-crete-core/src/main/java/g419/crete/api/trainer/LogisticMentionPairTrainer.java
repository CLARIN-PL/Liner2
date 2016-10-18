package g419.crete.api.trainer;

import g419.crete.api.instance.MentionPairClassificationInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

/**
 * Created by akaczmarek on 30.11.15.
 */
public class LogisticMentionPairTrainer extends AbstractCreteTrainer<Classifier, MentionPairClassificationInstance<Double>, Instance, Double> {

    @Override public Class<Classifier> getModelClass() {return Classifier.class;}
    @Override public Class<MentionPairClassificationInstance<Double>> getAbstractInstanceClass() {
        MentionPairClassificationInstance<Float> inst = new MentionPairClassificationInstance<>();
        return (Class<MentionPairClassificationInstance<Double>>) inst.getClass();
    }
    @Override public Class<Instance> getClassifierInstanceClass() {return Instance.class;}
    @Override public Class<Double> getLabelClass() {return Double.class;}
}
