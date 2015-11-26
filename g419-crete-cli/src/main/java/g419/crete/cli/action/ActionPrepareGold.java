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
import g419.crete.api.refine.CoverAnnotationDocumentRefiner;
import g419.crete.api.resolver.AbstractCreteResolver;
import g419.crete.api.resolver.factory.CreteResolverFactory;
import g419.crete.api.resolver.factory.NullResolverItem;
import g419.crete.api.resolver.factory.WekaJ48MentionPairResolverItem;
import g419.crete.api.resolver.factory.WekaJ48ResolverItem;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
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

	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
    public static final String NO_ANNOTATION_SELECTOR = "no_annotation_selector";

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
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
		AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(this.output_file, this.output_format);
        TokenFeatureGenerator gen = null;
        
        if(!CreteOptions.getOptions().getFeatures().get("token").isEmpty()){
        	gen = new TokenFeatureGenerator(CreteOptions.getOptions().getFeatures().get(TOKENS));
        }
       // Selektor wzmianek do usunięcia przed przetwarzaniem
		AbstractAnnotationSelector preFilterSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR));
		CoverAnnotationDocumentRefiner refiner = new CoverAnnotationDocumentRefiner(AnnotationSelectorFactory.getFactory().getInitializedSelector(NO_ANNOTATION_SELECTOR));
        Document ps = reader.nextDocument();
        while ( ps != null ){
			if ( gen != null ) gen.generateFeatures(ps);
			ps = refiner.refineDocument(ps);
			ps.filterAnnotationClusters(preFilterSelector.selectAnnotations(ps));
			ps.removeAnnotations(preFilterSelector.selectAnnotations(ps));
			// Zapisz wynikowy dokument
			writer.writeDocument(ps);
			// Przejdź do następnego dokumentu
			ps = reader.nextDocument();
		}
        
		reader.close();
		writer.close();
	}

}
