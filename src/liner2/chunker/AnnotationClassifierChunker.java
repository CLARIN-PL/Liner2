package liner2.chunker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liner2.features.AnnotationFeatureGenerator;
import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToNominal;

public class AnnotationClassifierChunker extends Chunker 
	implements TrainableChunkerInterface, DeserializableChunkerInterface, SerializableChunkerInterface {
	
	private AnnotationFeatureGenerator featureGenerator = null;
	private Chunker inputChunker = null;
	
	/**
	 * To be serialized.
	 */
	private Classifier classifier = null;
	private FastVector classes = null;
	private StringToNominal filter = null;
    private List <String> features;
	
	/**
	 * 
	 * @param inputChunker
	 */
    public AnnotationClassifierChunker(Chunker inputChunker, List <String> features) {
    	
    	this.features = features;
    	this.featureGenerator = new AnnotationFeatureGenerator(features);
    	this.inputChunker = inputChunker;
    }

    /**
     * 
     * @return
     */
    private FastVector getAttributesDefinition(){
    	FastVector attributes = new FastVector(
    			this.featureGenerator.getFeaturesCount()+1);
    	FastVector fvnull = null;
    	
    	for ( int i = 0; i<this.featureGenerator.getFeaturesCount(); i++ ){    		
    		attributes.addElement(new Attribute("attr" + i, fvnull));
    	}    	    	
    	attributes.addElement(new Attribute("class", this.classes));
	
    	return attributes;
    }
    
    /**
     * 
     * @param paragraphSet
     */
    private void setupClasses(ParagraphSet paragraphSet){
    	// Create a list of possible classes on the basis on training set
    	Set<String> classes = new HashSet<String>();
    	FastVector fvClasses = new FastVector();
    	for ( Sentence sentence : paragraphSet.getSentences() ){
    		for (Annotation ann : sentence.getChunks()){
    			if ( !classes.contains(ann.getType()) ){
    				classes.add(ann.getType());
    				fvClasses.addElement(ann.getType());
    			}
    		}
    	}
    	this.classes = fvClasses;    	
    }
    
    @Override
	public void train(ParagraphSet paragraphSet) throws Exception {
    	this.setupClasses(paragraphSet);
    	    	
    	FastVector fva = this.getAttributesDefinition();
    	Instances instances = new Instances("ner", fva, 10);
    	instances.setClassIndex(this.featureGenerator.getFeaturesCount());
    	    	
    	for ( Sentence sentence : paragraphSet.getSentences() ){

            List<HashMap<Annotation,String>> sentenceFeatures = this.featureGenerator.generate(sentence);

    		for (Annotation ann : sentence.getChunks()){
	    		List<String> annotationFeatures = this.featureGenerator.generate(ann);
	    		Instance instance = new Instance(this.featureGenerator.getFeaturesCount() + 1);

	    		for (int i=0; i<annotationFeatures.size(); i++){
	    			instance.setValue((Attribute)fva.elementAt(i), annotationFeatures.get(i));
	    		}
                for (int i=annotationFeatures.size(); i<sentenceFeatures.size(); i++){
                     instance.setValue((Attribute)fva.elementAt(i+annotationFeatures.size()),sentenceFeatures.get(i).get(ann));
                }
	    		instance.setValue((Attribute)fva.elementAt(this.featureGenerator.getFeaturesCount()),
	    				ann.getType());
	    		instances.add(instance);
    		}
    	}

    	this.filter = new StringToNominal();
    	filter.setAttributeRange("first-" + this.featureGenerator.getFeaturesCount());
    	filter.setInputFormat(instances);
    	for ( int i=0; i<instances.numInstances(); i++)
    		filter.input(instances.instance(i));
    	
    	filter.batchFinished();
    	
    	Instances instancesFiltered = filter.getOutputFormat();
    	Instance filtered = null;
    	while ( (filtered = filter.output()) != null )
    		instancesFiltered.add(filtered);
    	    	
    	Classifier classifier = new J48();
    	classifier.setDebug(true);
    	classifier.buildClassifier(instancesFiltered);
    	this.classifier = classifier;    	
    }

    /**
     * Wczytuje chunker z modelu binarnego.
     * @param model_filename
     */
	@Override
    public void deserialize(String model_filename){
		try {
			FileInputStream stream = new FileInputStream(model_filename);
			ObjectInputStream in = new ObjectInputStream(stream);
			this.classes = (FastVector) in.readObject();
			this.classifier = (Classifier) in.readObject();
            this.filter = (StringToNominal) in.readObject();
			in.close();
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void serialize(String filename) {
		try {
			FileOutputStream stream = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(stream);
			out.writeObject(this.classes);
			out.writeObject(this.classifier);
            out.writeObject(this.filter);
			out.close();
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	@Override
	public HashMap<Sentence, AnnotationSet> chunk(ParagraphSet ps) {
		HashMap<Sentence, AnnotationSet> inputChunks = this.inputChunker.chunk(ps);
		
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		for ( AnnotationSet as : inputChunks.values() )
			annotations.addAll(as.chunkSet());
		
		System.out.println("Annotations : " + annotations.size());
		
		try {
			this.classify(annotations);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return inputChunks;
	}

	/**
	 * Classify each annotation in the collection and set the annotation category.
	 * 
	 * @param annotations — collection of annotations
	 * @throws Exception 
	 */
	public void classify(List<Annotation> annotations) throws Exception{
    	FastVector fva = this.getAttributesDefinition();
    	Instances instances = new Instances("ner", fva, 10);
    	instances.setClassIndex(this.featureGenerator.getFeaturesCount());

		for ( Annotation ann : annotations ){
    		List<String> annotationFeatures = this.featureGenerator.generate(ann);
    		Instance instance = new Instance(annotationFeatures.size() + 1);
    		for (int i=0; i<annotationFeatures.size(); i++){
    			instance.setValue((Attribute)fva.elementAt(i), annotationFeatures.get(i));
    		}
    		instances.add(instance);
		}

    	for ( int i=0; i<instances.numInstances(); i++)
    		filter.input(instances.instance(i));
    	
    	filter.batchFinished();
    	
    	Instance filtered = null;
    	Attribute classAttribute = filter.getOutputFormat().classAttribute();
    	int index = 0;
    	int zero = 0;
    	while ( (filtered = filter.output()) != null ){
//    	for ( int i=0; i<instances.numInstances(); i++){
//    		Instance instance = instances.instance(i);
    		Instance instance = filtered;
    		double d = this.classifier.classifyInstance(instance);
    		if ( d < 1.0 ) zero++;
    		annotations.get(index++).setType(classAttribute.value((int)d));
    	 }    	
    	System.out.println("Zero : " + zero);
	}
}
