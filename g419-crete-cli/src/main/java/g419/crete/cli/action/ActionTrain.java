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
import g419.crete.api.classifier.factory.item.WekaRandomForestClassifierItem;
import g419.crete.api.classifier.factory.item.WekaLogisticRegressionClassifierItem;
import g419.crete.api.classifier.factory.item.WekaSmoClassifierItem;
import g419.crete.api.classifier.serialization.Serializer;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.MentionPairClassificationInstance;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.converter.factory.item.MentionPairClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.generator.ClusterClassificationInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.api.instance.generator.MentionPairInstanceGenerator;
import g419.crete.api.refine.CoverAnnotationDocumentRefiner;
import g419.crete.api.trainer.AbstractCreteTrainer;
import g419.crete.api.trainer.factory.CreteTrainerFactory;
import g419.crete.api.trainer.factory.LogisticMentionPairTrainerItem;
import g419.crete.api.trainer.factory.WekaJ48ClusterMentionTrainerItem;
import g419.crete.api.trainer.factory.WekaJ48MentionPairTrainerItem;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import weka.core.Instance;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ActionTrain extends Action {

	public static final String TOKENS = "token";
	public static final String ANNOTATIONS = "annotation";
	public static final String ANNOTATION_PAIRS = "annotation_pair";
	public static final String CLUSTERS = "cluster";
	public static final String CLUSTER_MENTION_PAIRS = "annotation_cluster";
	
	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
	public static final String BASIC_SELECTOR = "selector";
	public static final String SINGLETON_SELECTOR = "singleton_selector";

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
		// --------------- TRAINERS -----------------------------------
		CreteTrainerFactory.getFactory().register("randomforest_cluster_classify", new WekaJ48ClusterMentionTrainerItem());
		CreteTrainerFactory.getFactory().register("randomforest_mention_pair_classify", new WekaJ48MentionPairTrainerItem());
		CreteTrainerFactory.getFactory().register("logistic_mention_pair_classify", new LogisticMentionPairTrainerItem());
		// --------------- CLASSIFIERS -----------------------------------
		ClassifierFactory.getFactory().register("randomforest_cluster", new WekaRandomForestClassifierItem());
		ClassifierFactory.getFactory().register("randomforest_mention_pair", new WekaRandomForestClassifierItem());
		ClassifierFactory.getFactory().register("logistic_mention_pair", new WekaLogisticRegressionClassifierItem());
		ClassifierFactory.getFactory().register("logistic_mention_pair_smo", new WekaSmoClassifierItem());
//		ClassifierFactory.getFactory().register("lemur_ranking", new LemurClassifierItem());
		// ------------------ GENERATORS -------------------------------
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Integer.class, "mention_pair_generator", new MentionPairInstanceGenerator(1.0, -1.0, true));
		CreteInstanceGeneratorFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Double.class, "logistic_mention_pair_generator", new MentionPairInstanceGenerator(1.0, -1.0, true));
		// ----------------- CONVERTERS --------------------------------
		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
		CreteInstanceConverterFactory.getFactory().registerInstance(MentionPairClassificationInstance.getCls(), Instance.class, "mention_pair_to_weka_instance", new MentionPairClassificationWekaInstanceConverterItem());
	}
	
	
	@Override
	public void run() throws Exception {
		//---------------------- INITIALIZE GENERAL OPTIONS -----------------------------
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(input_file, input_format);
		AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        TokenFeatureGenerator gen = null;
        CreteOptions.getOptions();
        CreteOptions.getOptions().getFeatures();
        CreteOptions.getOptions().getFeatures().get(TOKENS);
        CreteOptions.getOptions().getFeatures().get(TOKENS).isEmpty();
        if(!CreteOptions.getOptions().getFeatures().get("token").isEmpty()){
        	gen = new TokenFeatureGenerator(CreteOptions.getOptions().getFeatures().get(TOKENS));
        }

		//---------------------- INITIALIZE FEATURES -----------------------------
        ArrayList<String> features = new ArrayList<String>();
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATION_PAIRS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATIONS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTERS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTER_MENTION_PAIRS).values());

		//---------------------- INITIALIZE CRETE FRAMEWORK -----------------------------
        initializeTrainers();

		//---------------------- INITIALIZE TRAINER -----------------------------
        String trainerName = CreteOptions.getOptions().getProperties().getProperty("trainer");
		String classifierName = CreteOptions.getOptions().getProperties().getProperty("classifier");
		String generatorName = CreteOptions.getOptions().getProperties().getProperty("generator");
		String converterName = CreteOptions.getOptions().getProperties().getProperty("converter");
        AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer(trainerName, classifierName, generatorName, converterName, features);

		//---------------------- INITIALIZE SELECTORS-----------------------------
		// Selector for discarding given annotations from training documents
        AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
		// Selector for annotations for which the coreference relation will be considered
        AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
		// Selector for overriding th
        AbstractAnnotationSelector singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));

		//---------------------- INITIALIZE DOCUMENT REFINER -----------------------------
		CoverAnnotationDocumentRefiner refiner = new CoverAnnotationDocumentRefiner(preFilterSelector);// args:prefilterSelector

		//---------------------- PROCESS DOCUMENTS -----------------------------
		Document ps = reader.nextDocument();


        while ( ps != null ){
            System.out.println(ps.getName());
            //---------------------- PREPROCESS DOCUMENT -----------------------------
            if ( gen != null ) gen.generateFeatures(ps);
			ps = refiner.refineDocument(ps);

            trainer.addDocumentTrainingInstances(ps, selector, singletonSelector);

			// Przejdź do następnego dokumentu
			ps = reader.nextDocument();
		}
        
		trainer.train();
		Serializer<?> trainedModel = trainer.getTrainedModel();
		trainedModel.persist(this.classifier_file);
		
		reader.close();
		writer.close();
	}
}
