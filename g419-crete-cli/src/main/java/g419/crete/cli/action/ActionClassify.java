package g419.crete.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.classifier.factory.ClassifierFactory;
import g419.crete.api.classifier.factory.item.WekaJ48ClassifierItem;
import g419.crete.api.classifier.serialization.WekaModelSerializer;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.MentionPairClassificationInstance;
import g419.crete.api.instance.converter.factory.CreteInstanceConverterFactory;
import g419.crete.api.instance.converter.factory.item.ClusterClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.converter.factory.item.MentionPairClassificationWekaInstanceConverterItem;
import g419.crete.api.instance.generator.ClusterClassificationInstanceGenerator;
import g419.crete.api.instance.generator.CreteInstanceGeneratorFactory;
import g419.crete.api.instance.generator.MentionPairInstanceGenerator;
import g419.crete.api.resolver.AbstractCreteResolver;
import g419.crete.api.resolver.factory.CreteResolverFactory;
import g419.crete.api.resolver.factory.NullResolverItem;
import g419.crete.api.resolver.factory.WekaJ48MentionPairResolverItem;
import g419.crete.api.resolver.factory.WekaJ48ResolverItem;
import g419.crete.api.resolver.factory.WekaRandomForestMentionPairClusterClassifyItem;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.Action;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import weka.core.Instance;

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
	public static final String OVERRIDE_SELECTOR = "override_selector";
	public static final String SINGLETON_SELECTOR = "singleton_selector";
	
	public static final String PERSON_NAM_SELECTOR = "person_nam_selector";
	public static final String PERSON_NAM_IN_SELECTOR = "person_nam_in_selector";

	
//	public static final String MODEL_PATH = "model_path";
	
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

	public void initializeResolvers(){
		CreteResolverFactory.getFactory().register("j48_cluster_classify", new WekaJ48ResolverItem());
		CreteResolverFactory.getFactory().register("j48_mention_pair_classify", new WekaJ48MentionPairResolverItem());
		CreteResolverFactory.getFactory().register("randomforest_mentionpair_cluster_classify", new WekaRandomForestMentionPairClusterClassifyItem());
		CreteResolverFactory.getFactory().register("null_resolver",  new NullResolverItem());
		// --------------- CLASSIFIERS -----------------------------------
		ClassifierFactory.getFactory().register("j48_cluster", new WekaJ48ClassifierItem());
		ClassifierFactory.getFactory().register("j48_mention_pair", new WekaJ48ClassifierItem());
		// ------------------ GENERATORS -------------------------------
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Integer.class, "mention_cluster_classify_generator", new ClusterClassificationInstanceGenerator());
		CreteInstanceGeneratorFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Integer.class, "mention_pair_generator", new MentionPairInstanceGenerator(false));
		// ----------------- CONVERTERS --------------------------------
		CreteInstanceConverterFactory.getFactory().registerInstance(ClusterClassificationInstance.class, Instance.class, "mention_cluster_to_weka_instance", new ClusterClassificationWekaInstanceConverterItem());
		CreteInstanceConverterFactory.getFactory().registerInstance(MentionPairClassificationInstance.class, Instance.class, "mention_pair_to_weka_instance", new MentionPairClassificationWekaInstanceConverterItem());
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
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATION_PAIRS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(ANNOTATIONS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTERS).values());
        features.addAll(CreteOptions.getOptions().getFeatures().get(CLUSTER_MENTION_PAIRS).values());
        
        initializeResolvers();
		
//        String modelPath = CreteOptions.getOptions().getProperties().getProperty(MODEL_PATH);
        WekaModelSerializer model = new WekaModelSerializer(null);
        model.load(this.classifier_file);
	
        // Instantiate resolver
		String resolverName = CreteOptions.getOptions().getProperties().getProperty("resolver");
		String classifierName = CreteOptions.getOptions().getProperties().getProperty("classifier");
		String generatorName = CreteOptions.getOptions().getProperties().getProperty("generator");
		String converterName = CreteOptions.getOptions().getProperties().getProperty("converter");
        AbstractCreteResolver<?, ?, ?, ?> resolver = CreteResolverFactory.getFactory().getResolver(resolverName, classifierName, generatorName, converterName, features, model);
		
		// Quick Fix for NamedEntities
//		resolver = CreteResolverFactory.getFactory().getResolver("j48_mention_pair_classify", "j48_mention_pair", "mention_pair_generator", "mention_pair_to_weka_instance", features, model);
//		resolver = new NullResolver()
		
		// Instantiate resolvers
		String[] resolverNames = new String[]{}; //temp
		List<AbstractCreteResolver<?, ?, ?, ?>> resolvers = new ArrayList<>();
		for(String resolverName2 : resolverNames){
			// TODO:
		}
		

		// Selektor wzmianek do usunięcia przed przetwarzaniem
		AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
		// Selektory nazw osób
		AbstractAnnotationSelector personNamSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PERSON_NAM_SELECTOR));
        AbstractAnnotationSelector personNamInSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PERSON_NAM_IN_SELECTOR));
        // Selector of mentions to be classified 
        // Selektor wzmianek do klasyfikacji
        AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
        // Selector of mentions to be overriden with basic selector annotations
        // Selektor do nadpisywania relacji ????
        AbstractAnnotationSelector overrideSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(OVERRIDE_SELECTOR));
        // Selector of singleton mentions to be considered as singleton clusters
        // Selektor singletonów --- do tworzenia klastrów zawierających pojedyncze wzmianki
        AbstractAnnotationSelector singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));
        
        // Read document
//        Document ps = reader.nextDocument();
//        while(ps != null){
//	        // Prepare document for classification
//	        // 1. Remove undesired annotations
//	        // 1. Usuwanie niepożądanych wzmianek --- przed wszystkimi resolverami
//	        ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
//	        // 2. Refine the person names annotations
//	        // 2. Poprawianie zagnieżdżeń w nazwach osób
//	        ps.refinePersonNamRelations(true);
//	        // 3. Process document through all resolvers
//	        // 3. Przetwórz dokument resolverami koreferencji
//	        for(AbstractCreteResolver<?, ?, ?, ?>  documentResolver : resolvers){
//	        	// Selector of mentions to be classified 
//	            // Selektor wzmianek do klasyfikacji
//	            AbstractAnnotationSelector selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(BASIC_SELECTOR));
//	            // Selector of mentions to be overriden with basic selector annotations
//	            // Selektor do nadpisywania relacji ????
//	            AbstractAnnotationSelector overrideSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(OVERRIDE_SELECTOR));
//	            // Selector of singleton mentions to be considered as singleton clusters
//	            // Selektor singletonów --- do tworzenia klastrów zawierających pojedyncze wzmianki
//	            AbstractAnnotationSelector singletonSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(SINGLETON_SELECTOR));
//	            if(overrideSelector != null){
//	            	// Rewire relations on mentions under consideration
//	                // Przepnij relacje na rozważanych wzmiankach
//	                ps = rewireRelations(ps, selector, overrideSelector, false);
//	                // Remove from clusters all mentions not to be classified
//	                // Usuń z klastrów relacyjnych wszystkie wzmianki, które nie będą klasyfikowane
//	                ps.filterAnnotationClusters(overrideSelector.selectAnnotations(ps));
//	            }
//	            else{
//	            	ps.filterAnnotationClusters(selector.selectAnnotations(ps));
//	            }
//	            
//	        	
//	            // Resolve coreference
//	            // Rozwiąż koreferencję
//	        	documentResolver.resolveDocument(ps, selector, singletonSelector);
//	        }
//	        
//	        // 4. Save document
//	        // 4. Zapisz dokument
//	        writer.writeDocument(ps);
//	        
//	        // 5. Go to next document
//	        // 5. Idź do następnego dokumentu
//	        ps = reader.nextDocument();
//        }
        
        Document ps = reader.nextDocument();
        while ( ps != null ){
			if ( gen != null ) gen.generateFeatures(ps);
			// Usuń niepożądane anotacje
			List<Annotation> preFilteredAnnotations = preFilterSelector.selectAnnotations(ps);
			ps.removeAnnotations(preFilteredAnnotations);
			// Przepnij relacje i usuń nazwy własne wewnętrzne nam_liv_person_* wewnątrz nam_liv_person
			ps.refinePersonNamRelations(true);
//			ps = rewireRelations(ps, personNamInSelector, personNamSelector, true);
			if(overrideSelector != null){
				// Przepnij relacje na rozważanych wzmiankach
				ps = rewireRelations(ps, selector, overrideSelector, false);
				// Usuń z klastrów wszystkie wzmianki, które będą klasyfikowane
				ps.filterAnnotationClusters(overrideSelector.selectAnnotations(ps));
				// Znajdź odpowiednie relacje koreferencji
				ps = resolver.resolveDocument(ps, overrideSelector, singletonSelector);
			}
			else{
				// Usuń z klastrów wszystkie wzmianki, które będą klasyfikowane
				ps.filterAnnotationClusters(selector.selectAnnotations(ps));
				// Znajdź odpowiednie relacje koreferencji
				ps = resolver.resolveDocument(ps, selector, singletonSelector);
			}
			// Oddaj relacje usunięte przed klasyfikacją relacji
			HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>();
			ps.getParagraphs().forEach(p -> p.getSentences()	.forEach(s -> chunkings.put(s, new AnnotationSet(s))));
			preFilteredAnnotations.forEach(a -> chunkings.get(a.getSentence()).addChunk(a));
			ps.addAnnotations(chunkings);
			// Zapisz wynikowy dokument
			writer.writeDocument(ps);
			// Przejdź do następnego dokumentu
			ps = reader.nextDocument();
		}
        
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

    // FIXME: zostają relacje do nieistniejących anotacji !!!
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
     * Get document reader defined with the -i and -f options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentReader getInputReader() throws Exception{
        return ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
    }
}
