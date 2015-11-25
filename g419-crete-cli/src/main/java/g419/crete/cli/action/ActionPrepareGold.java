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
import g419.lib.cli.CommonOptions;
import g419.lib.cli.Action;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.ArrayList;
import java.util.List;

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
public class ActionPrepareGold extends Action {
	
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

	
	public static final String MODEL_PATH = "model_path";
	
	private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;
	
    
    public ActionPrepareGold() {
		super("prepareGold");

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

       // Selektor wzmianek do usunięcia przed przetwarzaniem
		AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
		
        Document ps = reader.nextDocument();
        while ( ps != null ){
			if ( gen != null ) gen.generateFeatures(ps);
			// Usuń niepożądane anotacje
			ps.filterAnnotationClusters(preFilterSelector.selectAnnotations(ps));
			ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
			// Przepnij relacje i usuń nazwy własne wewnętrzne nam_liv_person_* wewnątrz nam_liv_person
			ps.refinePersonNamRelations(true);
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
