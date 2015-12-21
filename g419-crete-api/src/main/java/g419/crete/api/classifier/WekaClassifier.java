package g419.crete.api.classifier;

import g419.crete.api.classifier.serialization.Serializer;
import g419.crete.api.classifier.serialization.WekaModelSerializer;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.factory.FeatureFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import weka.core.*;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.converters.ArffSaver;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;

public abstract class WekaClassifier extends AbstractCreteClassifier<Classifier, Instance, Double>{
	protected Instances instances;
	protected Classifier classifier;
	protected Filter resampler;
	public Instances getInstancesCopy(){return new Instances(instances);}
	
	protected ArrayList<Attribute> attributes;
	public ArrayList<Attribute> getAttributes(){
		return attributes;
	}
	protected HashMap<String, Integer> attributesByNames;
	public Integer getAttributeIndex(String name){
		return attributesByNames.get(name);
	}
	
	public WekaClassifier(List<String> features, Filter resampler) {
		this.resampler = resampler;

		if(this.model != null && this.classifier == null) this.classifier = this.model.getModel();

		attributes = new ArrayList<>();
		attributesByNames = new HashMap<>();
		constructAttributes(features);
		
		FastVector fvWekaAttributes = new FastVector(attributes.size());
		for(Attribute attr : attributes) fvWekaAttributes.addElement(attr);
		instances = new Instances("Coref", fvWekaAttributes, trainingInstances.size());
	}
	
	private void constructAttributes(List<String> features){
		int attrIndex = 0;
		for(String featureName : features){
			AbstractFeature<?,?> feature = FeatureFactory.getFactory().getFeature(featureName);
			Attribute attr = constructAttribute(feature);
			attributes.add(attr);
			attributesByNames.put(feature.getName(), attrIndex++);
		}
		
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("NON_COREF");
		fvClassVal.addElement("COREF");
		Attribute classAttr = new Attribute("CLASS", fvClassVal);
		attributes.add(classAttr);
		attributesByNames.put("CLASS", attributes.size() - 1);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Attribute constructAttribute(AbstractFeature<?,?> feature){
		if(Enum.class.isAssignableFrom(feature.getReturnTypeClass())){
			FastVector fvEnumValues = new FastVector(feature.getSize());
			
			for(Object e : feature.getAllValues())
				fvEnumValues.addElement(e.toString());
			
			return new Attribute(feature.getName(), fvEnumValues);
		}
		if(String.class.isAssignableFrom(feature.getReturnTypeClass())){
			return new Attribute(feature.getName(), (FastVector)null);
		}
		else{
			return new Attribute(feature.getName());
		}
	}

	protected Filter getResampler() throws Exception{
		return resampler != null ? resampler : new AllFilter();
	}

	protected void displayDebugInfo(Classifier cModel, Instances tInstances) throws Exception{
		Evaluation eTest = new Evaluation(tInstances);
		eTest.evaluateModel(cModel, tInstances);
		String strSummary = eTest.toSummaryString();
		System.out.println(strSummary);
		System.out.println(cModel.toString());
		// Get the confusion matrix
		System.out.println(eTest.toMatrixString());
	}

	protected void exportInstances(Instances instances, String path) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File(path));
		saver.writeBatch();
	}

	protected Filter[] getFilters(Instances dataset) throws Exception{
		// Numeric to nominal filter
		NumericToNominal numToNom =  new NumericToNominal();
		numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});

		// String to nominal filter
		StringToNominal stringToNom = new StringToNominal();

		int[] rangeArray = IntStream.range(0, dataset.numAttributes())
				.parallel()
				.filter(index -> dataset.classIndex() != index)
				.filter(index -> dataset.attribute(index).isString())
				.toArray();

		String rangeString = Range.indicesToRangeList(rangeArray);
		stringToNom.setAttributeRange(rangeString);

		return new Filter[]{numToNom, stringToNom, getResampler()};
	}

	/**
	 * Prepare dataset from provided training instances and instances containing informations about attributes
	 * @return Instances - dataset prepared for classification
	 */
	protected Instances prepareDataset(){
		Instances dataset = getInstancesCopy();
		dataset.setClass(attributes.get(attributes.size() - 1));
		for(int i : IntStream.range(0, this.trainingInstances.size()).toArray()) {
			Instance instance = this.trainingInstances.get(i);
			instance.setDataset(dataset);
			instance.setClassValue(this.trainingInstanceLabels.get(i) > 0.0 ? "COREF" : "NON_COREF");
			dataset.add(instance);
		}

		return dataset;
	}

	/**
	 * Filters given dataset with provided set of filters
	 * @param dataset - dataset to be filtered
	 * @param filters - provided set of filters
	 * @return Instances - dataset prepared for classification
	 */
	protected Instances filterDataset(Instances dataset, Filter[] filters){
		// Filter dataset
		// - filter attributes (numeric to nominal, string to nominal etc.)
		// - perform resampling
		try {
//			exportInstances(tInstances, "/home/akaczmarek/data/rawinstances_named_merge.arff");
			for(Filter filter : filters) {
				filter.setInputFormat(dataset);
				dataset = Filter.useFilter(dataset, filter);
			}
//			tInstances = Filter.useFilter(instances, getResampler(tInstances));
			exportInstances(dataset, "/home/akaczmarek/data/rawinstances_named_merge_filtered.arff");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return dataset;
	}

	@Override
	public void train() {
		try{
			// Prepare dataset
			Instances tInstances = filterDataset(prepareDataset(), getFilters(instances));
			classifier.buildClassifier(tInstances);
			displayDebugInfo(classifier, tInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Serialize model
		this.model = new WekaModelSerializer(classifier);
	}

	@Override
	public Double classify(Instance instance){
		try{
			Instances dataset = new Instances(this.instances);
			dataset.add(instance);
			dataset.setClass(attributes.get(attributes.size() - 1));
			return classifier.classifyInstance(dataset.instance(0));
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<Double> classify(List<Instance> instances) {
		Instances clasInst = new Instances(this.instances);
		for(Instance instance : instances) clasInst.add(instance);
		clasInst.setClass(attributes.get(attributes.size() - 1));

		List<Double> labels = IntStream.range(0, clasInst.numInstances())
				.parallel()
				.mapToObj(index -> instances.get(index))
				.map(instance -> classify(instance))
				.collect(Collectors.toList());

		return labels;
	}

	public void crossValidate(int folds){
		try {
			Instances tInstances = filterDataset(prepareDataset(), getFilters(instances));
			// Create seeded number generator
			Random rand = new Random();
			// Create evaluator
			Evaluation eval = new Evaluation(tInstances);
			// Prepare folds
			tInstances.randomize(rand);
			tInstances.stratify(folds);

			for (int n = 0; n < folds; n++) {
				// Prepare instances
				Instances train = tInstances.trainCV(folds, n);
				Instances test = tInstances.testCV(folds, n);

				// Prepare classifier
				Classifier cls = Classifier.makeCopy(classifier);

				// Train classifier
				cls.buildClassifier(train);
				// Evaluate classifier
				eval.evaluateModel(cls, test);

				// Display local debug
				Evaluation evalLocal = new Evaluation(test);
				evalLocal.evaluateModel(cls, test);
				displayEvaluationDebug(evalLocal);
			}

			displayEvaluationDebug(eval);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void displayEvaluationDebug(Evaluation eval) throws Exception{
		System.out.println(eval.toSummaryString());
		System.out.println(eval.toMatrixString());
		System.out.println(eval.fMeasure(1));
	}

	@Override
	public void setModel(Serializer<Classifier> model){
		this.model = model;
		this.classifier = this.model.getModel();
	}

	@Override public Class<Double> getLabelClass() {return Double.class;}
	@Override public Class<Instance> getInstanceClass() { return Instance.class;}
}
