package g419.crete.api.classifier.factory.item;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaLogisticRegressionClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;

import java.util.List;

/**
 * Created by akaczmarek on 30.11.15.
 */
public class WekaLogisticRegressionClassifierItem implements IClassifierFactoryItem<Classifier, Instance, Double> {
    @Override
    public AbstractCreteClassifier<Classifier, Instance, Double> createClassifier(List<String> features) {
        return new WekaLogisticRegressionClassifier(features);
    }
}
