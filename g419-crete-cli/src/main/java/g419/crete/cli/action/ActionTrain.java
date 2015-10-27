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
import g419.crete.api.classifier.serialization.Serializer;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.MentionPairClassificationInstance;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.converter.factory.item.MentionPairClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.generator.ClusterClassificationInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.api.instance.generator.MentionPairInstanceGenerator;
import g419.crete.api.trainer.AbstractCreteTrainer;
import g419.crete.api.trainer.factory.CreteTrainerFactory;
import g419.crete.api.trainer.factory.WekaJ48ClusterMentionTrainerItem;
import g419.crete.api.trainer.factory.WekaJ48MentionPairTrainerItem;
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
	public static final String ANNOTATION_PAIRS = "annotation_pair";
	public static final String CLUSTERS = "cluster";
	public static final String CLUSTER_MENTION_PAIRS = "annotation_cluster";
	
	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
	public static final String BASIC_SELECTOR = "selector";
	public static final String OVERRIDE_SELECTOR = "override_selector";
	public static final String SINGLETON_SELECTOR = "singleton_selector";
	
	public static final String PERSON_NAM_SELECTOR = "person_nam_selector";
	public static final String PERSON_NAM_IN_SELECTOR = "person_nam_in_selector";
	
	public static final String TRAINER_NAMES = "trainers";
//	public static final String MODEL_PATH = "model_path";
	
	private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;
    private String classifier_file = null;
	
	
	public ActionTrain() {
		super("train");
		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileFormatOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getClassifierModelFile());
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
        this.classifier_file = line.getOptionValue(CommonOptions.OPTION_CLASSIFIER_MODEL, "model.mdl");
        CreteOptions.getOptions().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
		
	}

	public void initializeTrainers(){
		CreteTrainerFactory.getFactory().register("j48_cluster_classify", new WekaJ48ClusterMentionTrainerItem());
		CreteTrainerFactory.getFactory().register("j48_mention_pair_classify", new WekaJ48MentionPairTrainerItem());
		// --------------- CLASSIFIERS -----------------------------------
		ClassifierFactory.getFactory().register("j48_cluster", new WekaJ48ClassifierItem());
		ClassifierFactory.getFactory().register("j48_mention_pair", new WekaJ48ClassifierItem());
//		ClassifierFactory.getFactory().register("lemur_ranking", new LemurClassifierItem());
		// ------------------ GENERATORS -------------------------------
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Integer.class, "mention_pair_generator", new MentionPairInstanceGenerator(true));
		// ----------------- CONVERTERS --------------------------------
		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
		CreteInstanceConverterFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Instance.class, "mention_pair_to_weka_instance", new MentionPairClassificationWekaInstanceConverterItem());
	}
	
	
	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = getInputReader();
		AbstractDocumentWriter writer = getOutputWriter();
        TokenFeatureGenerator gen = null;
        CreteOptions.getOptions();
        CreteOptions.getOptions().getFeatures();
        CreteOptions.getOptions().getFeatures().get(TOKENS);
        CreteOptions.getOptions().getFeatures().get(TOKENS).isEmpty();
        if(!CreteOptions.getOptions().getFeatures().get("token").isEmpty()){
        	gen = new TokenFeatureGenerator(CreteOptions.getOptions().getFeatures().get(TOKENS));
        }

        ArrayList<String> features = new ArrayList<String>();
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATION_PAIRS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATIONS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTERS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTER_MENTION_PAIRS).values());
        
        initializeTrainers();
        // Zero trainer
//        AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", features);
        String resolverName = CreteOptions.getOptions().getProperties().getProperty("resolver");
		String classifierName = CreteOptions.getOptions().getProperties().getProperty("classifier");
		String generatorName = CreteOptions.getOptions().getProperties().getProperty("generator");
		String converterName = CreteOptions.getOptions().getProperties().getProperty("converter");
        AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer(resolverName, classifierName, generatorName, converterName, features);
        
        
//        //  Instantiate trainers
// 		String[] trainerNames = CreteOptions.getOptions().getProperties().getProperty(TRAINER_NAMES).split(",");
// 		List<AbstractCreteTrainer<?, ?, ?, ?>> trainers = new ArrayList<>();
// 		for(String trainerName : trainerNames){
// 			String classifierName;
// 			String generatorName;
// 			String converterName;
// 			AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer(trainerName, classifierName, generatorName, converterName, features);
// 			trainers.add(trainer);
// 		}
        

        AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
        AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
        AbstractAnnotationSelector overrideSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(OVERRIDE_SELECTOR));
        AbstractAnnotationSelector singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));
        AbstractAnnotationSelector personNamSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PERSON_NAM_SELECTOR));
        AbstractAnnotationSelector personNamInSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PERSON_NAM_IN_SELECTOR));
        
        
//        Document ps = reader.nextDocument();
//        while ( ps != null ){
//        	// Generate document features for tokens
//        	if ( gen != null ) gen.generateFeatures(ps);
//        	// Remove undesired annotations
//        	ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
//        	// Refine the person named entities nested annotations
//        	ps.refinePersonNamRelations(true);
//        	
//        	//
//        	for(AbstractCreteTrainer<?, ?, ?, ?> trainer : trainers){
//        		AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
//                AbstractAnnotationSelector overrideSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(OVERRIDE_SELECTOR));
//                AbstractAnnotationSelector singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));
//                
//        	}
//        	
//        	ps = reader.nextDocument();
//		}
        	
        Document ps = reader.nextDocument();
        while ( ps != null ){
			if ( gen != null ) gen.generateFeatures(ps);
			// Usuń niepożądane anotacje
			ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
			// Przepnij relacje i usuń nazwy własne wewnętrzne nam_liv_person_* wewnątrz nam_liv_person
//			ps = rewireRelations(ps, personNamInSelector, personNamSelector, true);
			ps.refinePersonNamRelations(true);
			if(overrideSelector != null){
				// Przepnij relacje na rozważanych wzmiankach
				ps = rewireRelations(ps, selector, overrideSelector, false);
				// Utwórz i dodaj instancje uczące dla danego dokumentu
				trainer.addDocumentTrainingInstances(ps, overrideSelector, singletonSelector);
			}
			else{
				// Utwórz i dodaj instancje uczące dla danego dokumentu
				trainer.addDocumentTrainingInstances(ps, selector, singletonSelector);
			}
			
			// Przejdź do następnego dokumentu
			ps = reader.nextDocument();
		}
        
		trainer.train();
		Serializer trainedModel = trainer.getTrainedModel();
//		String modelPath = CreteOptions.getOptions().getProperties().getProperty(MODEL_PATH);
		trainedModel.persist(this.classifier_file);
		
		reader.close();
		writer.close();
		
//		System.out.println("Hello world");
	}
	
	// TODO: FIXME vide 00101768.xml
	private Document rewireRelations(Document document, AbstractAnnotationSelector relationalAnnotations, AbstractAnnotationSelector nonrelationalAnnotations, boolean removeNonRelational){
		List<Annotation> relAnnotations = relationalAnnotations.selectAnnotations(document);
		List<Annotation> targetAnnotations = nonrelationalAnnotations.selectAnnotations(document);
		
		List<Annotation> toRemove = new ArrayList<Annotation>();
		
		for(Annotation rAnn : relAnnotations){
			boolean found = false;
			for(Annotation potentialTarget : rAnn.getSentence().getChunks()){
				if(potentialTarget.getTokens().equals(rAnn.getTokens()) && targetAnnotations.contains(potentialTarget)){
					document.rewireSingleRelations(rAnn, potentialTarget);
					found = true;
				}
			}
			if(!found) {
				rAnn.setType("anafora_verb_null");
			}
			// TODO: FIXME: nie usuwaj anotacji, które nie mają odpowiednika, który je pokrywa !!!
			if(found && removeNonRelational) toRemove.add(rAnn);
		}
		
		if(removeNonRelational) document.removeAnnotations(toRemove);
		
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
