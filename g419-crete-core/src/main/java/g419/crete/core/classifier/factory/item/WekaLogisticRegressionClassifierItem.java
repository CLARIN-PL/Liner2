package g419.crete.core.classifier.factory.item;

import g419.crete.core.CreteOptions;
import g419.crete.core.classifier.AbstractCreteClassifier;
import g419.crete.core.classifier.WekaLogisticRegressionClassifier;
import g419.crete.core.classifier.factory.WekaFilterFactory;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.filters.Filter;

import java.util.List;

/**
 * Created by akaczmarek on 30.11.15.
 */
public class WekaLogisticRegressionClassifierItem implements IClassifierFactoryItem<Classifier, Instance, Double> {

    public static final String FILTER = "filter";

    @Override
    public AbstractCreteClassifier<Classifier, Instance, Double> createClassifier(List<String> features) {
        Filter resampler = WekaFilterFactory.getFactory().getFilter(CreteOptions.getOptions().getProperties().getProperty(FILTER));
        return new WekaLogisticRegressionClassifier(features, null);
    }
}
