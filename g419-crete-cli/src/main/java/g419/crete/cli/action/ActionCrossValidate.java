//package g419.crete.cli.action;
//
//import g419.corpus.io.reader.AbstractDocumentReader;
//import g419.corpus.io.reader.ReaderFactory;
//import g419.corpus.io.writer.AbstractDocumentWriter;
//import g419.corpus.io.writer.WriterFactory;
//import g419.corpus.structure.Document;
//import g419.crete.api.CreteOptions;
//import g419.crete.api.annotation.AbstractAnnotationSelector;
//import g419.crete.api.annotation.AnnotationSelectorFactory;
//import g419.crete.api.classifier.factory.ClassifierFactory;
//import g419.crete.api.classifier.factory.item.WekaJ48ClassifierItem;
//import g419.crete.api.classifier.serialization.Serializer;
//import g419.crete.api.instance.ClusterClassificationInstance;
//import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
//import g419.crete.api.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
//import g419.crete.api.instance.generator.ClusterClassificationInstanceGenerator;
//import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
//import g419.crete.api.resolver.AbstractCreteResolver;
//import g419.crete.api.resolver.factory.CreteResolverFactory;
//import g419.crete.api.resolver.factory.WekaJ48ResolverItem;
//import g419.crete.api.trainer.AbstractCreteTrainer;
//import g419.crete.api.trainer.factory.CreteTrainerFactory;
//import g419.crete.api.trainer.factory.WekaJ48ClusterMentionTrainerItem;
//import g419.lib.cli.CommonOptions;
//import g419.lib.cli.action.Action;
//import g419.liner2.api.features.TokenFeatureGenerator;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.DefaultParser;
//
//import weka.core.Instance;
//
///**
// * Akcja klasyfikacji koreferencji w dokumentach.
// * Dla zadanego dokumentu tworzy relacje pomiędzy wzmiankami - frazami oznaczonymi jako odnoszące się do nazwy własnej
// * oraz nazwami własnymi. Dodatkowo klasyfikuje także powiązania koreferencyjne pomiędzy samymi nazwami własnymi.
// *
// * @author Adam Kaczmarek
// *
// */
//public class ActionCrossValidate extends Action {
//
//	public static final String TOKENS = "token";
//	public static final String ANNOTATIONS = "annotation";
//	public static final String CLUSTERS = "cluster";
//	public static final String CLUSTER_MENTION_PAIRS = "annotation_cluster";
//
//	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
//	public static final String BASIC_SELECTOR = "selector";
//	public static final String OVERRIDE_SELECTOR = "override_selector";
//	public static final String SINGLETON_SELECTOR = "singleton_selector";
//
//	public static final String PERSON_NAM_SELECTOR = "person_nam_selector";
//	public static final String PERSON_NAM_IN_SELECTOR = "person_nam_in_selector";
//
//	public static final String MODEL_PATH = "model_path";
//
////	private String input_file = null;
////    private String input_format = null;
//    private String output_file = null;
//    private String output_format = null;
//
//    private TokenFeatureGenerator gen = null;
//
//    private AbstractDocumentWriter writer = null;
//
//    private AbstractAnnotationSelector preFilterSelector = null;
//    private AbstractAnnotationSelector selector = null;
//    private AbstractAnnotationSelector overrideSelector = null;
//    private AbstractAnnotationSelector singletonSelector = null;
//
//    private ArrayList<String> features;
//
//    public ActionCrossValidate() {
//		super("cv");
//
////		this.options.addOption(CommonOptions.getInputFileFormatOption());
////        this.options.addOption(CommonOptions.getInputFileNameOption());
//        this.options.addOption(CommonOptions.getOutputFileFormatOption());
//        this.options.addOption(CommonOptions.getOutputFileNameOption());
//        this.options.addOption(CommonOptions.getFeaturesOption());
////        this.options.addOption(CommonOptions.getFeaturesOption());
//        this.options.addOption(CommonOptions.getModelFileOption());
//	}
//
//	@Override
//	public void parseOptions(String[] args) throws Exception {
//		CommandLine line = new DefaultParser().parse(this.options, args);
//        parseDefault(line);
//        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
//        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
////        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
////        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
//        CreteOptions.getOptions().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
//
//	}
//
//	public void initialize(){
//		// ----------------- RESOLVER ----------------------------
//		CreteResolverFactory.getFactory().register("j48_cluster_classify", new WekaJ48ResolverItem());
//		// ----------------- TRAINER -------------------------------
//		CreteTrainerFactory.getFactory().register("j48_cluster_classify", new WekaJ48ClusterMentionTrainerItem());
//		// --------------- CLASSIFIERS -----------------------------------
//		ClassifierFactory.getFactory().register("j48_cluster", new WekaJ48ClassifierItem());
//		// ------------------ GENERATORS -------------------------------
//		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
//		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
//		// ----------------- CONVERTERS --------------------------------
//		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
//	}
//
//
//
//	/**
//	 * Przebieg klasyfikacji wszystkich relacji koreferencyjnych dla wszystkich dokumentów
//	 * @pattern TemplateMethod
//	 *
//	 */
//	@Override
//	public void run() throws Exception {
//		int numFolds = 10;
//		initialize();
//		this.writer = getOutputWriter();
//		this.features = new ArrayList<>();
//		this.features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATIONS).values());
//		this.features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTERS).values());
//		this.features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTER_MENTION_PAIRS).values());
//
//		this.preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
//        this.selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
//        this.overrideSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(OVERRIDE_SELECTOR));
//        this.singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));
//
//        AbstractAnnotationSelector personNamSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PERSON_NAM_SELECTOR));
//        AbstractAnnotationSelector personNamInSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PERSON_NAM_IN_SELECTOR));
//
//		if(!CreteOptions.getOptions().getFeatures().get("token").isEmpty()){
//        	this.gen = new TokenFeatureGenerator(CreteOptions.getOptions().getFeatures().get(TOKENS));
//        }
//
//		List<String> folds = Arrays.asList(new String[]{
//				"/home/adam/kpwr-inforex/fold_0.txt",
//				"/home/adam/kpwr-inforex/fold_1.txt",
//				"/home/adam/kpwr-inforex/fold_2.txt",
//				"/home/adam/kpwr-inforex/fold_3.txt",
//				"/home/adam/kpwr-inforex/fold_4.txt",
//				"/home/adam/kpwr-inforex/fold_5.txt",
//				"/home/adam/kpwr-inforex/fold_6.txt",
//				"/home/adam/kpwr-inforex/fold_7.txt",
//				"/home/adam/kpwr-inforex/fold_8.txt",
//				"/home/adam/kpwr-inforex/fold_9.txt"
//		});
//
//		List<List<Document>> foldsDocuments = loadFolds(folds);
//
//		// TODO: Preprocessing dokumentów
//		for(List<Document> foldDocuments : foldsDocuments){
//			for(Document document : foldDocuments){
//				// Usuń niepożądane anotacje
//				document.removeAnnotations(preFilterSelector.selectAnnotations(document));
//				// Przepnij relacje i usuń nazwy własne wewnętrzne nam_liv_person_* wewnątrz nam_liv_person
////				document.rewireRelations(personNamInSelector.selectAnnotations(document), personNamSelector.selectAnnotations(document), true);
//				document.refinePersonNamRelations(true);
//				// Przepnij relacje na rozważanych wzmiankach
//				if(overrideSelector != null) document.rewireRelations(selector.selectAnnotations(document), overrideSelector.selectAnnotations(document), false);
//			}
//		}
//
//		// Cross Validate
//		List<Serializer> models = new ArrayList<Serializer>();
//		// TRAIN MODELS
//		for(int i = 0; i < numFolds; i++)
//			models.add(trainFold(foldsDocuments, i, numFolds));
//
//		// CLASSIFY
//		for(int i = 0; i < numFolds; i++)
//			classifyFold(foldsDocuments, i, numFolds, models.get(i));
//
//		this.writer.close();
//	}
//
//	private void classifyFold(List<List<Document>> foldsDocuments, int foldNum, int numFolds, Serializer model){
//		List<Document> test = foldsDocuments.get(foldNum);
//		AbstractCreteResolver<?, ?, ?, ?> resolver = CreteResolverFactory.getFactory().getResolver("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", this.features, model);
//
//		for(Document testDocument: test){
//			// Usuń z klastrów wszystkie wzmianki, które będą klasyfikowane
//			Document ps = testDocument; // COPY !!!
//			ps.filterAnnotationClusters(overrideSelector.selectAnnotations(ps));
//			ps = resolver.resolveDocument(ps, overrideSelector, singletonSelector);
//			// Zapisz wynikowy dokument
//			this.writer.writeDocument(ps);
//		}
//	}
//
//	private Serializer<?> trainFold(List<List<Document>> foldsDocuments, int foldNum, int numFolds){
//		List<Document> train = new ArrayList<Document>();
//		AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", this.features);
//
//		for(int i = 0; i < numFolds; i++)
//			if(foldNum != i)
//				train.addAll(foldsDocuments.get(i));
//
//		for(Document trainDocument: train){
//			System.out.println("Adding instances: "+ trainDocument.getName());
//			trainer.addDocumentTrainingInstances(trainDocument, overrideSelector, singletonSelector);
//		}
//		trainer.train();
//		return trainer.getTrainedModel();
//	}
//
//	private void crossValidate(List<List<Document>> foldsDocuments, int foldNum){
//		List<Document> train = new ArrayList<Document>();
//		List<Document> test = new ArrayList<Document>();
//
//		for(int i = 0; i < 10; i++){
//			if(foldNum == i){
//				test.addAll(foldsDocuments.get(i));
//			}
//			else{
//				train.addAll(foldsDocuments.get(i));
//			}
//		}
//
//		// TRAIN
//		AbstractCreteTrainer<?, ?, ?, ?> trainer = CreteTrainerFactory.getFactory().getTrainer("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", this.features);
//		for(Document trainDocument: train){
//			System.out.println("Adding instances: "+ trainDocument.getName());
//			trainer.addDocumentTrainingInstances(trainDocument, overrideSelector, singletonSelector);
//		}
//		trainer.train();
//		Serializer trainedModel = trainer.getTrainedModel();
//		String modelPath = CreteOptions.getOptions().getProperties().getProperty(MODEL_PATH) + foldNum;
//		trainedModel.persist(modelPath);
//
//		// CLASSIFY
//		AbstractCreteResolver<?, ?, ?, ?> resolver = CreteResolverFactory.getFactory().getResolver("j48_cluster_classify", "j48_cluster", "mention_cluster_generator", "mention_cluster_to_weka_instance", this.features, trainedModel);
//		for(Document testDocument: test){
//			// Usuń z klastrów wszystkie wzmianki, które będą klasyfikowane
//			Document ps = testDocument; // COPY !!!
//			ps.filterAnnotationClusters(overrideSelector.selectAnnotations(ps));
//			ps = resolver.resolveDocument(ps, overrideSelector, singletonSelector);
//			// Zapisz wynikowy dokument
//			this.writer.writeDocument(ps);
//		}
//		this.writer.close();
//
//	}
//
//	private List<List<Document>> loadFolds(List<String> folds) throws Exception{
//		List<List<Document>> documentFolds = new ArrayList<List<Document>>();
//
//		for(String fold : folds) documentFolds.add(loadFold(fold));
//
//		return documentFolds;
//	}
//
//	private List<Document> loadFold(String foldFile) throws Exception{
//		List<Document> documents = new ArrayList<Document>();
//		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(foldFile, "batch:ccl_rel");
//
//
//		Document ps = reader.nextDocument();
//        while ( ps != null ){
//			if ( this.gen != null ) this.gen.generateFeatures(ps);
//			// Dodaj do listy dokumentów
//			documents.add(ps);
//			// Przejdź do następnego dokumentu
//			ps = reader.nextDocument();
//		}
//
//
//		return documents;
//	}
//
//	/**
//     * Get document writer defined with the -o and -t options.
//     * @return
//     * @throws Exception
//     */
//    protected AbstractDocumentWriter getOutputWriter() throws Exception{
//        AbstractDocumentWriter writer;
//
//        if (output_file == null){
//            writer = WriterFactory.get().getStreamWriter(System.out, output_format);
//        }
//        else if (output_format.equals("arff")){
////            ToDo: format w postaci arff:{PLIK Z TEMPLATEM}
//            writer = null;
////            CrfTemplate arff_template = LinerOptions.getGlobal().getArffTemplate();
////            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
//        }
//        else{
//            writer = WriterFactory.get().getStreamWriter(output_file, output_format);
//        }
//        return writer;
//    }
//
//}
