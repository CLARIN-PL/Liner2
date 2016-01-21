package g419.crete.api.classifier;

import g419.crete.api.classifier.WekaClassifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.filters.Filter;

import java.util.List;

/**
 * Created by akaczmarek on 17.12.15.
 */
public class WekaSmoClassifier extends WekaClassifier{

    public WekaSmoClassifier(List<String> features, Filter resampler, double C, double epsilon, Kernel kernel) {
        super(features, resampler);
        SMO smo = new SMO();
        smo.setC(C);
        smo.setEpsilon(epsilon);
        smo.setKernel(kernel);
        smo.setDebug(true);
        this.classifier = smo;
    }
}
