package g419.crete.api.classifier;

import weka.classifiers.functions.Logistic;
import weka.filters.Filter;

import java.util.List;

/**
 * Created by akaczmarek on 30.11.15.
 */
public class WekaLogisticRegressionClassifier extends WekaClassifier{

    public WekaLogisticRegressionClassifier(List<String> features, Filter resampler) {
        super(features, resampler);
        Logistic logistic = new Logistic();
        logistic.setDebug(true);
        this.classifier = logistic;
    }
}
