package g419.tools.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.WrappedToken;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.ValueComparator;
import g419.liner2.api.tools.parser.MaltParser;
import g419.liner2.api.tools.parser.MaltSentence;
import g419.liner2.api.tools.parser.TokenWrapper;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.sumo.WordnetToSumo;
import g419.tools.maltfeature.DependencyPath;
import g419.tools.maltfeature.MaltPattern;
import g419.tools.maltfeature.MaltPatternNode;

/**
 * Created by michal on 2/12/15.
 */
public class FeatureMatrix extends Tool{

    public static final String OPTION_MALT = "m";
    public static final String OPTION_MALT_LONG = "malt";

    public static final String OPTION_PATTERNS = "p";
    public static final String OPTION_PATTERNS_LONG = "patterns";

    public static final String OPTION_SENTENCES = "s";
    public static final String OPTION_SENTENCES_LONG = "sentences";

    public static final String OPTION_CATEGORY_MATRIX = "matrix";

    public static final String OPTION_CATEGORIZE = "c";
    public static final String OPTION_CATEGORIZE_LONG = "categorize";

    public static final String OPTION_LEMMATIZATION = "l";
    public static final String OPTION_LEMMATIZATION_LONG = "lemmatization";

    private String input_file = null;
    private String input_format = null;
    private String filename_patterns = null;
    private String patterns_output = null;
    private String categorize_output = null;
    private File category_matrix_file = null;
    private boolean getSentences = false;
    private MaltParser malt = null;
    private HashMap<String, Integer> lemma_count = new HashMap<>();
    private ArrayList<MaltPattern> patterns = new ArrayList<>();
    HashMap<String, HashMap<String, HashMap<String, Integer>>> results = new HashMap<>();
    HashMap<String, HashMap<String, Integer>> categoryMatrix = new HashMap<>();
    HashMap<String, String> lemmatized_names = new HashMap<>();
    HashSet<String> nominativeNames = new HashSet<>();
    WordnetToSumo serdel = null;

    public FeatureMatrix() {
        super("feature-matrix");
        this.setDescription("Generuje macierz wystąpień cech dla określonych klas anotacji.");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());

        this.options.addOption(
        		Option.builder(OPTION_MALT).longOpt(OPTION_MALT_LONG).hasArg().argName("malt")
        		.desc("path to maltparser model").required().build());

        this.options.addOption(
        		Option.builder(OPTION_PATTERNS).longOpt(OPTION_PATTERNS_LONG).hasArg().argName("patterns")
        		.desc("path to file with patterns").required().build());

        this.options.addOption(
        		Option.builder(OPTION_SENTENCES).longOpt(OPTION_SENTENCES_LONG)
        		.desc("create additional output with sentences for all names").build());

        this.options.addOption(
        		Option.builder(OPTION_CATEGORY_MATRIX).hasArg().argName("matrix")
        		.desc("output for matrix with frequency by categories").build());

        try {
			this.serdel = new WordnetToSumo();
		} catch (IOException | DataFormatException e) {
			Logger.getLogger(this.getClass()).error("Błąd wczytania WordnetToSumo: " + e.getMessage());
		}
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.patterns_output = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if(line.hasOption(OPTION_CATEGORY_MATRIX)){
            this.category_matrix_file = new File(line.getOptionValue(OPTION_CATEGORY_MATRIX));
        }
        if(line.hasOption(OPTION_CATEGORIZE)){
            this.categorize_output = line.getOptionValue(OPTION_CATEGORIZE);
            if(this.category_matrix_file == null || !this.category_matrix_file.exists()){
                throw new DataFormatException("category matrix data set is required for categorization");
            }
        }
        String modelPath = line.getOptionValue(OPTION_MALT);
        this.filename_patterns = line.getOptionValue(OPTION_PATTERNS);
        malt = new MaltParser(modelPath);
        

        if(line.hasOption(OPTION_SENTENCES)){
            getSentences = true;
        }
    }

    @Override
    public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
        Document ps = null;
        WordnetToSumo serdel = new WordnetToSumo();

        for ( String pattern : Files.readAllLines(Paths.get(this.filename_patterns) )){
        	pattern = pattern.trim();
        	if ( pattern.length() > 0 ){
        		MaltPattern p = new MaltPattern(pattern);
        		if ( p != null ){
        			this.patterns.add(p);
        		}
        		else{
        			Logger.getLogger(this.getClass()).error("Błąd parsowania wzorca: " + pattern);
        		}
        	}
        }
        
        while ( (ps = reader.nextDocument()) != null ){
            for(Sentence sent: ps.getSentences()){
                MaltSentence maltSent = new MaltSentence(sent, sent.getChunks());
                this.malt.parse(maltSent);
                //maltSent.wrapConjunctions();
                
                if ( maltSent.getAnnotations().size() > 0){
                    for (String str : maltSent.getMaltData()){
                    	System.out.println(str);
                    }                	
                }
                
                for (Annotation ann : maltSent.getAnnotations()) {
                	System.out.println(ann.toString());
                	int pathsCount = 0;
                    for (MaltPattern pattern : patterns) {
                    	List<DependencyPath> paths = pattern.match(maltSent, ann.getHead() );
                    	for ( DependencyPath path : paths ){
                    		System.out.println(pattern.getPatternString() + ":: " + path.toString(maltSent.getSentence()));
                    		System.out.println("PATTERN|" + this.pathToPattern(maltSent.getSentence(), pattern, path));
                    		System.out.println("PATTERN|" + this.pathToPattern(maltSent.getSentence(), pattern, path, serdel));
                    		pathsCount++;
                    	}                 
                    }
                	if ( pathsCount == 0 ){
                		System.out.println("NO_PATH");
                	}
                }
            }
        }
    }
    
    /**
     * 
     * @param sentence
     * @param pattern
     * @param path
     * @return
     */
    public String pathToPattern(Sentence sentence, MaltPattern pattern, DependencyPath path){
    	StringBuilder sb = new StringBuilder();
    	for ( int i=0; i<pattern.getNodes().size(); i++ ){
    		MaltPatternNode node = pattern.getNodes().get(i);
    		if ( "name".equals(node.getLabel()) ){
    			sb.append("name");
    		}
    		else if ( node.getLabel() == null ) {
    			sb.append(node.toString());
    		}
    		else{
    			int index = path.getMatchedNodes().get(node);
    			sb.append("base:" + sentence.getTokens().get(index).getDisambTag().getBase());
    		}
    		if ( i < pattern.getEdges().size() ){
    			sb.append(pattern.getEdges().get(i));
    		}
    	}
    	return sb.toString();
    }

    /**
     * 
     * @param sentence
     * @param pattern
     * @param path
     * @return
     */
    public String pathToPattern(Sentence sentence, MaltPattern pattern, DependencyPath path, WordnetToSumo sumo){
    	StringBuilder sb = new StringBuilder();
    	List<Set<String>> parts = new ArrayList<Set<String>>();
    	for ( int i=0; i<pattern.getNodes().size(); i++ ){
    		MaltPatternNode node = pattern.getNodes().get(i);
    		if ( "name".equals(node.getLabel()) ){
    			sb.append("name");
    			parts.add(new HashSet<String>(){{add("name");}});
    		}
    		else if ( node.getLabel() == null ) {
    			sb.append(node.toString());
    			parts.add(new HashSet<String>(){{add(node.toString());}});
    		}
    		else{
    			int index = path.getMatchedNodes().get(node);
    			String base = sentence.getTokens().get(index).getDisambTag().getBase();
    			Set<String> concepts = serdel.getConcept(base);
    			if ( concepts != null ){
    				Set<String> elements = new HashSet<String>();
    				for ( String concept : concepts ){
    					elements.add(concept);
    				}
    				parts.add(elements);
    				sb.append("sumo:" + concepts.iterator().next());
    			}
    			else{
    				sb.append("sumo:??" );
    				parts.add(new HashSet<String>(){{add("base:"+base);}});
    			}
    		}
    		if ( i < pattern.getEdges().size() ){
    			sb.append(pattern.getEdges().get(i));
    		}
    	}
    	return sb.toString();
    }


}
