package g419.liner2.cli.action;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import g419.corpus.ConsolePrinter;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.features.TokenFeatureGenerator;


/**
 * Searches for a phrases matching given pattern based on a set of token features.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionSearch extends Action{

	private static final String PARAM_PATTERN = "p";
	private static final String PARAM_PATTERN_LONG = "pattern";
	
    private String inputFile = null;
    private String inputFormat = null;
    private String featuresFile = null;  
    private String pattern = null;
    
	public ActionSearch() {
		super("search");
        this.setDescription("earches for a phrases matching given pattern based on a set of token features");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption(true));
        this.options.addOption(CommonOptions.getVerboseDeatilsOption());
        this.options.addOption(Option.builder(PARAM_PATTERN).longOpt(PARAM_PATTERN_LONG).hasArg().argName("pattern")
        		.desc("wzorzec jako sekwencja nazw cech").required().build());
	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        this.featuresFile = line.getOptionValue(CommonOptions.OPTION_FEATURES_LONG);
        this.pattern = line.getOptionValue(PARAM_PATTERN);
        if(line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)){
            ConsolePrinter.verboseDetails = true;
        }
	}
	
	/**
	 * 
	 */		
	public void run() throws Exception {

		LinkedHashMap<String, String> features = LinerOptions.getGlobal().parseFeatures(this.featuresFile);		
    	TokenFeatureGenerator gen = new TokenFeatureGenerator(features);

    	AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFile, this.inputFormat);
    	
    	Document document = null;

    	List<String> attributes = new ArrayList<String>();
    	for ( String item : this.pattern.split("[ +]")){
    		attributes.add(item);
    	}

    	while ( ( document = reader.nextDocument()) != null ){
    		gen.generateFeatures(document);
    		
    		for ( Sentence sentence : document.getSentences() ){
    			for ( int i=0; i<sentence.getTokenNumber(); i++ ){
    				if ( i + attributes.size() < sentence.getTokenNumber()){
    					StringJoiner joinerFeatures = new StringJoiner(" ");
    					StringJoiner joinerOrth = new StringJoiner(" ");
    					StringJoiner joinerLabels = new StringJoiner(" ");
    					for ( int j=0; j<attributes.size(); j++){
    						joinerOrth.add(sentence.getTokens().get(i+j).getOrth());
    						joinerLabels.add(sentence.getTokenClassLabel(i+j));
    						joinerFeatures.add(sentence.getTokens().get(i+j).getAttributeValue(attributes.get(j)));
    					}    					
    					System.out.println("Match # " + joinerFeatures + " # " + joinerOrth.toString() + " # " + joinerLabels );
    				}
    			}
    		}
    		
    	}

	}


}
