//package g419.crete.api.classifier;
//
//import g419.crete.api.classifier.serialization.WekaModelSerializer;
//import g419.crete.api.instance.ClusterClassificationInstance;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
//import weka.classifiers.trees.J48;
//import weka.classifiers.trees.RandomForest;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.Range;
//import weka.filters.Filter;
//import weka.filters.supervised.instance.Resample;
//import weka.filters.supervised.instance.SMOTE;
//import weka.filters.unsupervised.attribute.NumericToNominal;
//import weka.filters.unsupervised.attribute.StringToNominal;
//
//public class LabelRankingWekaClassifier extends WekaClassifier<ClusterClassificationInstance, Float> {
//
//	public LabelRankingWekaClassifier(List<String> features) {
//		super(features);
//	}
//
//	@Override
//	public void train() {
//		Instances tInstances = instances;
//		tInstances.setClass(attributes.get(attributes.size() - 1));
//
//		for(int i = 0; i < this.trainingInstances.size(); i++){
//			Instance instance = this.trainingInstances.get(i);
//			instance.setDataset(tInstances);
//			Float oldLabel = this.trainingInstanceLabels.get(i);
//			String newLabel = oldLabel > 0 ? "COREF" : "NON_COREF";
//			instance.setClassValue(newLabel);
//			tInstances.add(instance);
//		}
//
//		try {
////			exportInstances(tInstances, "/home/akaczmarek/data/rawinstances_named_merge.arff");
//			NumericToNominal numToNom =  new NumericToNominal();
//			numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
//			numToNom.setInputFormat(tInstances);
//
//			StringToNominal stringToNom = new StringToNominal();
//			ArrayList<Integer> rangeList = new ArrayList<>();
//			for (int i = 0; i < tInstances.numAttributes(); i++) {
//				if (tInstances.classIndex() == i) continue;
//				if (tInstances.attribute(i).isString()) rangeList.add(i);
//			}
//			int[] rangeArray = new int[rangeList.size()];
//			for(int i = 0; i < rangeList.size(); i++) rangeArray[i] = rangeList.get(i);
//
//			String rangeString = Range.indicesToRangeList(rangeArray);
//			stringToNom.setAttributeRange(rangeString);
//			stringToNom.setInputFormat(tInstances);
//
//			tInstances = Filter.useFilter(tInstances, numToNom);
//			tInstances = Filter.useFilter(tInstances, stringToNom);
//
////			exportInstances(tInstances, "/home/akaczmarek/data/rawinstances_named_merge_filtered.arff");
//
//			SMOTE smote = new SMOTE();
//			smote.setInputFormat(tInstances);
//			smote.setClassValue("0");
//
//			Resample resample = new Resample();
//			resample.setBiasToUniformClass(1.0);
//			resample.setSampleSizePercent(100);
//			resample.setInputFormat(tInstances);
//
//			tInstances = Filter.useFilter(tInstances, smote);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//
//		J48 j48 = new J48();
//		RandomForest rf = new RandomForest();
//		System.out.println(rf.getNumTrees());
//		rf.setNumTrees(200);
//
//		Classifier cModel = (Classifier) rf;
//		try {
//			cModel.buildClassifier(tInstances);
//			displayDebugInfo(cModel, tInstances);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
////		this.model = new WekaModelSerializer(classifier);
//	}
//
//	@Override
//	public Class<Float> getLabelClass() {
//		return null;
//	}
//
//	@Override
//	public List<Float> classify(List<Instance> instances) {
//		return null;
//	}
//
//	private void displayDebugInfo(Classifier cModel, Instances tInstances) throws Exception{
//		Evaluation eTest = new Evaluation(tInstances);
//		eTest.evaluateModel(cModel, tInstances);
//		String strSummary = eTest.toSummaryString();
//		System.out.println(strSummary);
//		System.out.println(cModel.toString());
//		// Get the confusion matrix
//		 System.out.println(eTest.toMatrixString());
//	}
//
//}
