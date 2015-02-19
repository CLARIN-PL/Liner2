package g419.crete.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.classifier.factory.ClassifierFactory;
import g419.crete.api.classifier.factory.item.WekaJ48ClassifierItem;
import g419.crete.api.classifier.model.Model;
import g419.crete.api.classifier.model.WekaModel;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.generator.AbstractCreteInstanceGenerator;
import g419.crete.api.instance.generator.ClusterClassificationInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.api.resolver.AbstractCreteResolver;
import g419.crete.api.resolver.factory.CreteResolverFactory;
import g419.crete.api.resolver.factory.WekaJ48ResolverItem;
import g419.crete.api.trainer.AbstractCreteTrainer;
import g419.crete.api.trainer.factory.CreteTrainerFactory;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

import weka.core.Instance;

/**
 * Akcja klasyfikacji koreferencji w dokumentach.
 * Dla zadanego dokumentu tworzy relacje pomiędzy wzmiankami - frazami oznaczonymi jako odnoszące się do nazwy własnej 
 * oraz nazwami własnymi. Dodatkowo klasyfikuje także powiązania koreferencyjne pomiędzy samymi nazwami własnymi.
 * 
 * @author Adam Kaczmarek
 *
 */
public class ActionCrossValidate extends Action {
	
	public static final String TOKENS = "token";
	public static final String ANNOTATIONS = "annotation";
	public static final String CLUSTERS = "cluster";
	public static final String CLUSTER_MENTION_PAIRS = "annotation_cluster";
	
	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
	public static final String BASIC_SELECTOR = "selector";
	
	public static final String MODEL_PATH = "model_path";
	
	private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;
	
    
    public ActionCrossValidate() {
		super("cv");

		this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileFormatOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
		CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        CreteOptions.getOptions().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
		
	}

	public void initializeResolvers(){
		CreteResolverFactory.getFactory().register("j48_cluster_classify", new WekaJ48ResolverItem());
		// --------------- CLASSIFIERS -----------------------------------
		ClassifierFactory.getFactory().register("j48_cluster", new WekaJ48ClassifierItem());
		// ------------------ GENERATORS -------------------------------
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
		// ----------------- CONVERTERS --------------------------------
		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
	}
	
	
	/**
	 * Przebieg klasyfikacji wszystkich relacji koreferencyjnych dla wszystkich dokumentów
	 * @pattern TemplateMethod
	 * 
	 */
	
	
	
	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = getInputReader();
		AbstractDocumentWriter writer = getOutputWriter();
        TokenFeatureGenerator gen = null;
        
        if(!CreteOptions.getOptions().getFeatures().get("token").isEmpty()){
        	gen = new TokenFeatureGenerator(CreteOptions.getOptions().getFeatures().get(TOKENS));
        }

        ArrayList<String> features = new ArrayList<String>();
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATIONS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTERS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTER_MENTION_PAIRS).values());
        
        initializeResolvers();
		
        String modelPath = CreteOptions.getOptions().getProperties().getProperty(MODEL_PATH);
        WekaModel model = new WekaModel(null);
        model.load(modelPath);
		// Instantiate resolver
		
		AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
        AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
        
        List<Document> documents = new ArrayList<Document>();
        Document ps = reader.nextDocument();
        while ( ps != null ){
			if ( gen != null ) gen.generateFeatures(ps);
			ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
			documents.add(ps);
			ps = reader.nextDocument();
		}
        
        AbstractCreteInstanceGenerator<?, ?> generator = CreteInstanceGeneratorFactory.getFactory().getInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ArrayList<String>());
        this.generateFolds(10, documents, selector, generator);
        
		reader.close();
		writer.close();
	}
	
	/**
     * Get document writer defined with the -o and -t options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentWriter getOutputWriter() throws Exception{
        AbstractDocumentWriter writer;

        if ( output_format.startsWith("batch:") && !input_format.startsWith("batch:") ) {
            throw new Exception("Output format `batch:` (-o) is valid only for `batch:` input format (-i).");
        }
        if (output_file == null){
            writer = WriterFactory.get().getStreamWriter(System.out, output_format);
        }
        else if (output_format.equals("arff")){
//            ToDo: format w postaci arff:{PLIK Z TEMPLATEM}
            writer = null;
//            CrfTemplate arff_template = LinerOptions.getGlobal().getArffTemplate();
//            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        }
        return writer;
    }

    /**
     * Get document reader defined with the -i and -f options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentReader getInputReader() throws Exception{
        return ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
    }
    

    public ArrayList<ArrayList<Document>> generateFolds(int foldNumber,  List<Document> documents, AbstractAnnotationSelector selector, AbstractCreteInstanceGenerator<?, ?> generator){
    	ArrayList<ArrayList<Document>> folds = new ArrayList<ArrayList<Document>>(foldNumber);
    	HashMap<Integer, Set<Document>> docsByCount = new HashMap<Integer, Set<Document>>();
    	HashMap<Document, Integer> countByDocs = new  HashMap<Document, Integer>(documents.size());
    	int totalInstances = 0;
    	
    	// Prepare data
    	for(Document document : documents){
    		int docInstances = generator.generateInstances(document, selector).size();
    		totalInstances += docInstances;
    		countByDocs.put(document, docInstances);
    		
    		if(docsByCount.get(docInstances) == null){
    			docsByCount.put(docInstances, new HashSet<Document>());
    		}
    		docsByCount.get(docInstances).add(document);
    		
//    		Set<Document> currentSet = docsByCount.get(docInstances);
//    		currentSet.add(document);
//    		docsByCount.put(docInstances, currentSet);
    	}
    	
    	// Fill folds
    	double averageFoldSize = ((double) totalInstances) / ((double) foldNumber);
    	// Shuffle documents list
    	long seed = System.nanoTime();
    	Collections.shuffle(documents, new Random(seed));
    	
    	// Fill folds
    	int currentFoldCount = 0;
    	int currentFoldIndex = 0;
    	folds.add(new ArrayList<Document>());
    	for(Document document : documents){
    		int documentInstanceCount = countByDocs.get(document);
    		// Check for overflow
    		if(currentFoldCount + documentInstanceCount > averageFoldSize){
    			// Stop if filled all folds
    			if(currentFoldIndex + 1 >= foldNumber) break;
    			// Switch to next fold
    			currentFoldIndex++;
    			currentFoldCount = 0;
    			folds.add(new ArrayList<Document>());
    		}
    		currentFoldCount += documentInstanceCount;
    		folds.get(currentFoldIndex).add(document);
    	}
    	
    	// Distribute rest of documents
    	// Sort folds by count - reverse
    	// Sort documents by count
    	
    	
    	return folds;
    }
    
    public List<Document> crossValidate(ArrayList<ArrayList<Document>> folds){
    	List<Document> classifiedDocuments = null;    	
    	
    	
    	return classifiedDocuments;
    }
    
//    public List<Document> cvSingleFold(List<Document> train, List<Document> test, List<String> features, AbstractAnnotationSelector selector){
//    	List<Document> classifiedTest = new ArrayList<Document>();
//    	
//    	// TRAINING PART
//    	AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", features);
//    	
//    	for(Document document: train) trainer.addDocumentTrainingInstances(document, selector);
//    	
//    	trainer.train();
//    	Model mdl = trainer.getTrainedModel();
//    	
//    	// CLASSIFICATION PART
//    	AbstractCreteResolver<?, ?, ?, ?> resolver = CreteResolverFactory.getFactory().getResolver("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", features, mdl); 
//		
////    	for(Document document : test) classifiedTest.add(resolver.resolveDocument(document, selector));
//    }
    
    
}
