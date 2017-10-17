package g419.crete.core.classifier.factory.item;

import g419.crete.core.CreteOptions;
import g419.crete.core.classifier.AbstractCreteClassifier;

import java.util.HashMap;
import java.util.List;

import g419.crete.core.classifier.WekaSmoClassifier;
import g419.crete.core.classifier.factory.WekaFilterFactory;
import weka.classifiers.Classifier;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.core.Instance;
import weka.filters.Filter;

public class WekaSmoClassifierItem implements IClassifierFactoryItem<Classifier, Instance, Double>{

    public static final String FILTER = "filter";

    protected double getDoubleParameterOrDefault(String parameterName, double defaultValue){
        try{
            return Double.valueOf(CreteOptions.getOptions().getClassifierParameters().getOrDefault(parameterName, "" + defaultValue));
        }
        catch(Exception e){
            e.printStackTrace();
            return defaultValue;
        }
    }

    @Override
    public AbstractCreteClassifier<Classifier, Instance, Double> createClassifier(List<String> features) {
        double C = getDoubleParameterOrDefault("C", 1.0);
        double epsilon = getDoubleParameterOrDefault("epsilon", 1e-12);
        PolyKernel kernel = new PolyKernel();
        kernel.setExponent(getDoubleParameterOrDefault("exponent", 2.0));
        Filter resampler = WekaFilterFactory.getFactory().getFilter(CreteOptions.getOptions().getProperties().getProperty(FILTER));
        return new WekaSmoClassifier(features, null, C, epsilon, kernel);
    }

}
