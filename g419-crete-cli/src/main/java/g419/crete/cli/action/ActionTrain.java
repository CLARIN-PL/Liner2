package g419.crete.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.classifier.factory.ClassifierFactory;
import g419.crete.api.classifier.factory.item.WekaJ48ClassifierItem;
import g419.crete.api.classifier.model.Model;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.generator.ClusterClassificationInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.api.trainer.AbstractCreteTrainer;
import g419.crete.api.trainer.factory.CreteTrainerFactory;
import g419.crete.api.trainer.factory.WekaJ48TrainerItem;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import weka.core.Instance;

public class ActionTrain extends Action {

	public static final String TOKENS = "token";
	public static final String ANNOTATIONS = "annotation";
	public static final String CLUSTERS = "cluster";
	public static final String CLUSTER_MENTION_PAIRS = "annotation_cluster";
	
	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
	public static final String BASIC_SELECTOR = "selector";
	public static final String OVERRIDE_SELECTOR = "override_selector";
	
	public static final String MODEL_PATH = "model_path";
	
	private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;
	
	
	public ActionTrain() {
		super("train");
		
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
		CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        CreteOptions.getOptions().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
		
	}

	public void initializeTrainers(List<String> features){
		CreteTrainerFactory.getFactory().register("j48_cluster_classify", new WekaJ48TrainerItem());
		// --------------- CLASSIFIERS -----------------------------------
		ClassifierFactory.getFactory().register("j48_cluster", new WekaJ48ClassifierItem());
		// ------------------ GENERATORS -------------------------------
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
		// ----------------- CONVERTERS --------------------------------
		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
	}
	
	
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
        
        initializeTrainers(features);
        AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", features);
        

        AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
        AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
        AbstractAnnotationSelector overrideSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(OVERRIDE_SELECTOR));
        
        Document ps = reader.nextDocument();
        while ( ps != null ){
			if ( gen != null ) gen.generateFeatures(ps);
			ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
			if(overrideSelector != null) ps = rewireRelations(ps, selector, overrideSelector);
			trainer.addDocumentTrainingInstances(ps, overrideSelector);
			ps = reader.nextDocument();
		}
        
		trainer.train();
		Model trainedModel = trainer.getTrainedModel();
		String modelPath = CreteOptions.getOptions().getProperties().getProperty(MODEL_PATH);
		trainedModel.persist(modelPath);
		
		reader.close();
		writer.close();
		
//		System.out.println("Hello world");
	}
	
	private Document rewireRelations(Document document, AbstractAnnotationSelector relationalAnnotations, AbstractAnnotationSelector nonrelationalAnnotations){
		List<Annotation> relAnnotations = relationalAnnotations.selectAnnotations(document);
		List<Annotation> targetAnnotations = nonrelationalAnnotations.selectAnnotations(document);
		
		for(Annotation rAnn : relAnnotations){
			for(Annotation potentialTarget : rAnn.getSentence().getChunks()){
				if(potentialTarget.getTokens().equals(rAnn.getTokens()) && targetAnnotations.contains(potentialTarget)){
					document.rewireSingleRelations(rAnn, potentialTarget);
				}
			}
		}
		
		return document;
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

}
