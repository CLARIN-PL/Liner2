package g419.crete.api.classifier;

import g419.crete.api.classifier.serialization.WekaModelSerializer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akaczmarek on 30.11.15.
 */
public class WekaLogisticRegressionClassifier extends WekaClassifier<Classifier, Double>{

    public WekaLogisticRegressionClassifier(List<String> features) {
        super(features);
    }

    @Override
    public List<Double> classify(List<Instance> instances) {
        Instances clasInst = new Instances(this.instances);
        for(Instance instance : instances) clasInst.add(instance);
        clasInst.setClass(attributes.get(attributes.size() - 1));

        ArrayList<Double> labels = new ArrayList<>();
        Classifier cls = this.model.getModel();

        for(int i = 0; i < clasInst.numInstances(); i++){
            Instance instance = clasInst.instance(i);
            try {
                labels.add(cls.distributionForInstance(instance)[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return labels;
    }

    @Override
    public void train() {
        Instances tInstances = instances;
        tInstances.setClass(attributes.get(attributes.size() - 1));

        for(int i = 0; i < this.trainingInstances.size(); i++){
            Instance instance = this.trainingInstances.get(i);
            instance.setDataset(tInstances);
            Double oldLabel = this.trainingInstanceLabels.get(i);
            String newLabel = oldLabel > 0 ? "COREF" : "NON_COREF";
            instance.setClassValue(newLabel);
            tInstances.add(instance);
        }

        try {
            NumericToNominal numToNom =  new NumericToNominal();
            numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
            numToNom.setInputFormat(tInstances);

            StringToNominal stringToNom = new StringToNominal();
            ArrayList<Integer> rangeList = new ArrayList<>();
            for (int i = 0; i < tInstances.numAttributes(); i++) {
                if (tInstances.classIndex() == i) continue;
                if (tInstances.attribute(i).isString()) rangeList.add(i);
            }
            int[] rangeArray = new int[rangeList.size()];
            for(int i = 0; i < rangeList.size(); i++) rangeArray[i] = rangeList.get(i);

            String rangeString = Range.indicesToRangeList(rangeArray);
            stringToNom.setAttributeRange(rangeString);
            stringToNom.setInputFormat(tInstances);

            tInstances = Filter.useFilter(tInstances, numToNom);
            tInstances = Filter.useFilter(tInstances, stringToNom);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        SMO smo = new SMO();
        PolyKernel polyKernel = new PolyKernel();
        polyKernel.setExponent(2);
        smo.setKernel(polyKernel);
        smo.setDebug(true);

        Logistic logistic = new Logistic();
        logistic.setDebug(true);
        Classifier cModel = (Classifier) smo;

        try {
            cModel.buildClassifier(tInstances);
            displayDebugInfo(cModel, tInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.model = new WekaModelSerializer(cModel);


    }

    private void displayDebugInfo(Classifier cModel, Instances tInstances) throws Exception{
        Evaluation eTest = new Evaluation(tInstances);
        eTest.evaluateModel(cModel, tInstances);
        String strSummary = eTest.toSummaryString();
        System.out.println(strSummary);
        System.out.println(cModel.toString());
        // Get the confusion matrix
        System.out.println(eTest.toMatrixString());
    }

    @Override
    public Class<Double> getLabelClass() { return Double.class; }
}
