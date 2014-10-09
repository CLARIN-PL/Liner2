package g419.liner2.api.chunker;

import g419.corpus.Logger;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.features.AnnotationFeatureGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.regex.Pattern;

import weka.classifiers.Classifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToNominal;

/**
 * TODO: wymaga aktualizacji dla rozdzielenia train na addTrainingData i train.
 * @author czuk
 *
 */
public class AnnotationClassifierChunker extends Chunker 
	implements TrainableChunkerInterface, DeserializableChunkerInterface, SerializableChunkerInterface {
	
	private AnnotationFeatureGenerator featureGenerator = null;
	private Chunker inputChunker = null;
	
	/**
	 * To be serialized.
	 */
	private Classifier classifier = null;
	private FastVector classes = new FastVector();
	private StringToNominal filter = null;
    private List <String> features;
    List<Pattern> types;
    FastVector fva = null;
    Instances instances = null;
	
	/**
	 * 
	 * @param inputChunker
	 */
    public AnnotationClassifierChunker(Chunker inputChunker, List <String> features) {
        this.features = features;
        this.featureGenerator = new AnnotationFeatureGenerator(features);
        this.inputChunker = inputChunker;
    }

    public void setTypes(List<Pattern> types){
        this.types = types;
    }

    public void initializeTraining(String classifierName, String[] classifierOptions, String strategy){
        try {
            if (strategy.equals("1-vs-all")){
                classifier = new MultiClassClassifier();
                classifier.setOptions(new String[]{"-W", classifierName});
            }
            else if (strategy.equals("multi")){
                classifier = Classifier.forName(classifierName, classifierOptions);
            }
            else{
                throw new Exception("Invalid classifier strategy: "+strategy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.fva = this.getAttributesDefinition();
        this.instances = new Instances("ner", fva, 10);
        instances.setClassIndex(this.featureGenerator.getFeaturesCount());
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
    public void updateClassDomain(Document paragraphSet){
    	// Create a list of possible classes on the basis on training set
        for ( Sentence sentence : paragraphSet.getSentences() ){
            for (Annotation ann : sentence.getChunks()){
                if ( !this.classes.contains(ann.getType()) ) {
                    if (types != null && !types.isEmpty()) {
                        for (Pattern patt : types) {
                            if (patt.matcher(ann.getType()).find()) {
                                this.classes.addElement(ann.getType());
                                break;
                            }
                        }
                    }
                    else{
                        this.classes.addElement(ann.getType());
                    }
                }

            }
    	}
    }
    
    @Override
	public void train() throws Exception {
        Logger.log("AnnoatationClassifier: training model");
    	this.filter = new StringToNominal();
    	filter.setAttributeRange("first-" + this.featureGenerator.getFeaturesCount());
    	filter.setInputFormat(instances);
    	for ( int i=0; i<instances.numInstances(); i++)
    		filter.input(instances.instance(i));

    	filter.batchFinished();

    	Instances instancesFiltered = filter.getOutputFormat();
    	Instance filtered;
    	while ( (filtered = filter.output()) != null ) {
            instancesFiltered.add(filtered);
        }

    	this.classifier.buildClassifier(instancesFiltered);
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
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> inputChunks = this.inputChunker.chunk(ps);
		try {
			this.classify(inputChunks);
		} catch (Exception e) {
			e.printStackTrace();
            System.exit(1);
		}
		
		return inputChunks;
	}

	/**
	 * Classify each annotation in the collection and set the annotation category.
	 * 
	 * @param annotationsBySentence — collection of AnnotationSet's grouped by sentences
	 * @throws Exception 
	 */
	public void classify(HashMap<Sentence, AnnotationSet> annotationsBySentence) throws Exception{
        Logger.log("AnnoatationClassifier: classifying data");
        if(fva == null){
            fva = this.getAttributesDefinition();
        }
        Instances instances = new Instances("ner", fva, 10);
        instances.setClassIndex(this.featureGenerator.getFeaturesCount());

        List<Annotation> allAnnotations = new ArrayList<Annotation>();
        for ( Sentence sentence : annotationsBySentence.keySet() ){

            List<HashMap<Annotation,String>> sentenceFeatures = this.featureGenerator.generate(sentence, annotationsBySentence.get(sentence).chunkSet());

            instanceLoop:
            for (Annotation ann : annotationsBySentence.get(sentence).chunkSet()){
                List<String> annotationFeatures = this.featureGenerator.generate(ann);
                Instance instance = new Instance(this.featureGenerator.getFeaturesCount() + 1);

                for (int i=0; i<annotationFeatures.size(); i++){
                    instance.setValue((Attribute)fva.elementAt(i), annotationFeatures.get(i));
                }
                for (int i=0; i<sentenceFeatures.size(); i++){
                    if(!sentenceFeatures.get(i).containsKey(ann)){
                        continue instanceLoop;
                    }
                    allAnnotations.add(ann);
                    instance.setValue((Attribute)fva.elementAt(i+annotationFeatures.size()), sentenceFeatures.get(i).get(ann));
                }
                instances.add(instance);
            }
        }

    	for ( int i=0; i<instances.numInstances(); i++)
    		filter.input(instances.instance(i));
    	
    	filter.batchFinished();
    	
    	Instance filtered = null;
    	Attribute classAttribute = filter.getOutputFormat().classAttribute();
    	int index = 0;
    	int zero = 0;
    	while ( (filtered = filter.output()) != null ){
    		Instance instance = filtered;
    		double d = this.classifier.classifyInstance(instance);
    		if ( d < 1.0 ) zero++;
            allAnnotations.get(index++).setType(classAttribute.value((int)d));
        }
	}

	@Override
	public void addTrainingData(Document document) throws Exception {
        Logger.log("AnnoatationClassifier: Loading training data for from document:" + document.getName());
        updateClassDomain(document);
        for ( Sentence sentence : document.getSentences() ){
            List<HashMap<Annotation,String>> sentenceFeatures = this.featureGenerator.generate(sentence, sentence.getChunks());
            instanceLoop:
            for (Annotation ann : sentence.getChunks()){
                if(this.classes.contains(ann.getType())){
                    List<String> annotationFeatures = this.featureGenerator.generate(ann);
                    Instance instance = new Instance(this.featureGenerator.getFeaturesCount() + 1);
                    instance.setDataset(instances);

                    for (int i=0; i<annotationFeatures.size(); i++){
                        instance.setValue((Attribute) fva.elementAt(i), annotationFeatures.get(i));
                    }
                    for (int i=0; i<sentenceFeatures.size(); i++){
                        if(!sentenceFeatures.get(i).containsKey(ann)){
                            continue instanceLoop;
                        }
                        instance.setValue((Attribute) fva.elementAt(i + annotationFeatures.size()), sentenceFeatures.get(i).get(ann));
                    }
                    instance.setClassValue(ann.getType());
                    instances.add(instance);
                }
            }
        }
		
	}
	
}
