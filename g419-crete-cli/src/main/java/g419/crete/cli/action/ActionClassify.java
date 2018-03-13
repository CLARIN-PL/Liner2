package g419.crete.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.crete.core.CreteOptions;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.annotation.AnnotationSelectorFactory;
import g419.crete.core.classifier.factory.ClassifierFactory;
import g419.crete.core.classifier.factory.item.WekaRandomForestClassifierItem;
import g419.crete.core.classifier.factory.item.WekaLogisticRegressionClassifierItem;
import g419.crete.core.classifier.factory.item.WekaSmoClassifierItem;
import g419.crete.core.classifier.serialization.WekaModelSerializer;
import g419.crete.core.instance.ClusterClassificationInstance;
import g419.crete.core.instance.MentionPairClassificationInstance;
import g419.crete.core.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.core.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
import g419.crete.core.instance.converter.factory.item.MentionPairClassificationWekaInstanceConverterItem;
import g419.crete.core.instance.generator.ClusterClassificationInstanceGenerator;
import g419.crete.core.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.core.instance.generator.MentionPairInstanceGenerator;
import g419.crete.core.refine.CoverAnnotationDocumentRefiner;
import g419.crete.core.resolver.AbstractCreteResolver;
import g419.crete.core.resolver.factory.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Akcja klasyfikacji koreferencji w dokumentach.
 * Dla zadanego dokumentu tworzy relacje pomiędzy wzmiankami - frazami oznaczonymi jako odnoszące się do nazwy własnej 
 * oraz nazwami własnymi. Dodatkowo klasyfikuje także powiązania koreferencyjne pomiędzy samymi nazwami własnymi.
 * 
 * @author Adam Kaczmarek
 *
 */
public class ActionClassify extends Action {

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


	public ActionClassify() {
		super("classify");
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
	public void parseOptions(final CommandLine line) throws Exception {
		this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
		this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
		this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
		this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
		this.classifier_file = line.getOptionValue(CommonOptions.OPTION_CLASSIFIER_MODEL, "model.mdl");
		CreteOptions.getOptions().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}

	public void initializeResolvers() {
		CreteResolverFactory.getFactory().register("randomforest_cluster_classify", new WekaJ48ResolverItem());
		CreteResolverFactory.getFactory().register("randomforest_mention_pair_classify", new WekaJ48MentionPairResolverItem());
		CreteResolverFactory.getFactory().register("randomforest_mentionpair_cluster_classify", new WekaRandomForestMentionPairClusterClassifyItem());
		CreteResolverFactory.getFactory().register("null_resolver", new NullResolverItem());
		// --------------- CLASSIFIERS -----------------------------------
		ClassifierFactory.getFactory().register("randomforest_cluster", new WekaRandomForestClassifierItem());
		ClassifierFactory.getFactory().register("randomforest_mention_pair", new WekaRandomForestClassifierItem());
		ClassifierFactory.getFactory().register("logistic_mention_pair", new WekaLogisticRegressionClassifierItem());
		ClassifierFactory.getFactory().register("logistic_mention_pair_smo", new WekaSmoClassifierItem());
		// ------------------ GENERATORS -------------------------------
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Double.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Double.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Double.class, "mention_pair_generator", new MentionPairInstanceGenerator(1.0, -1.0, false));
		CreteInstanceGeneratorFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Double.class, "logistic_mention_pair_generator", new MentionPairInstanceGenerator(1.0, -1.0, true));
		// ----------------- CONVERTERS --------------------------------
		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
		CreteInstanceConverterFactory.getFactory().registerInstance(MentionPairClassificationInstance.getCls(), Instance.class, "mention_pair_to_weka_instance", new MentionPairClassificationWekaInstanceConverterItem());
	}


	/**
	 * Przebieg klasyfikacji wszystkich relacji koreferencyjnych dla wszystkich dokumentów
	 *
	 * @pattern TemplateMethod
	 */
	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
		AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(this.output_file, this.output_format);
		TokenFeatureGenerator gen = null;

		if (!CreteOptions.getOptions().getFeatures().get("token").isEmpty()) {
			gen = new TokenFeatureGenerator(CreteOptions.getOptions().getFeatures().get(TOKENS));
		}

		ArrayList<String> features = new ArrayList<String>();
		features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATION_PAIRS).values());
		features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATIONS).values());
		features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTERS).values());
		features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTER_MENTION_PAIRS).values());

		initializeResolvers();

		WekaModelSerializer model = new WekaModelSerializer(null);
		model.load(this.classifier_file);

		// Instantiate resolver
		String resolverName = CreteOptions.getOptions().getProperties().getProperty("resolver");
		String classifierName = CreteOptions.getOptions().getProperties().getProperty("classifier");
		String generatorName = CreteOptions.getOptions().getProperties().getProperty("generator");
		String converterName = CreteOptions.getOptions().getProperties().getProperty("converter");
		AbstractCreteResolver<?, ?, ?, ?> resolver = CreteResolverFactory.getFactory().getResolver(resolverName, classifierName, generatorName, converterName, features, model);

		// Selektor wzmianek do usunięcia przed przetwarzaniem
		AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
		// Selector of mentions to be classified
		// Selektor wzmianek do klasyfikacji
		AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
		// Selector of singleton mentions to be considered as singleton clusters
		// Selektor singletonów --- do tworzenia klastrów zawierających pojedyncze wzmianki
		AbstractAnnotationSelector singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));

		//---------------------- INITIALIZE DOCUMENT REFINER -----------------------------
		CoverAnnotationDocumentRefiner refiner = new CoverAnnotationDocumentRefiner(preFilterSelector);

		//---------------------- PROCESS DOCUMENT -----------------------------
		Document ps = reader.nextDocument();
		while (ps != null) {
			//---------------------- PREPROCESS DOCUMENT ----------------------------
			if (gen != null) gen.generateFeatures(ps);
			List<Annotation> preFilteredAnnotations = preFilterSelector.selectAnnotations(ps);
			ps = refiner.refineDocument(ps);
			ps.filterAnnotationClusters(selector.selectAnnotations(ps));

			//---------------------- CLASSIFY/RESOLVE DOCUMENT ----------------------------
			ps = resolver.resolveDocument(ps, selector, singletonSelector);

			//---------------------- POSTPROCESS DOCUMENT ----------------------------

			// Oddaj relacje usunięte przed klasyfikacją relacji
			HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>();
			ps.getParagraphs().forEach(p -> p.getSentences().forEach(s -> chunkings.put(s, new AnnotationSet(s))));
			preFilteredAnnotations.forEach(a -> chunkings.get(a.getSentence()).addChunk(a));
			ps.addAnnotations(chunkings);

			//---------------------- SAVE DOCUMENT ----------------------------
			writer.writeDocument(ps);

			//---------------------- GO TO NEXT DOCUMENT ----------------------------
			ps = reader.nextDocument();
		}

		reader.close();
		writer.close();
	}
}
