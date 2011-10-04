package liner2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import liner2.filter.*;
import liner2.chunker.factory.ChunkerFactory;
import liner2.reader.FeatureGenerator;
import liner2.tools.CorpusFactory;
import liner2.tools.ParameterException;
import liner2.tools.TemplateFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

/**
 * This class handles module parameters. The parameters are read from
 * console and from ini files.
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 *
 */
public class LinerOptions {

	/**
	 * There is one LinetOptions object. 
	 */
	static private final LinerOptions linerOptions = new LinerOptions();
	static protected final String PARAM_PRINT = ">> Param: %20s = %s ";
	static protected final String paramDefPrint = ">> Param: %20s = %s (as default)";
	
	/**
	 * Read-only access to the LinerOptions 
	 * @return
	 */
	public static LinerOptions get(){
		return linerOptions;
	}
	
	/**
	 * Get property value
	 */
	public static String getOption(String option) {
		return linerOptions.getProperties().getProperty(option);
	}
	
	public static void requireOption(String option) throws ParameterException {
		// TODO not implemented yet
	}
        
	private Options options = null;
	private String configurationDescription = "";
	private Properties properties;
	
	private static final String LOCAL_INI = "local.ini";
	
	public static final String OPTION_CHUNKER = "chunker";
	public static final String OPTION_COMMON = "common";
	public static final String OPTION_CORPUS = "corpus";
	public static final String OPTION_FEATURE = "feature";
	public static final String OPTION_FILTER = "filter";
	public static final String OPTION_HELP = "help";
	public static final String OPTION_HEURISTICS = "heuristics";
	public static final String OPTION_INI = "ini";
	public static final String OPTION_INPUT_FILE = "f";
	public static final String OPTION_INPUT_FORMAT = "i";
	public static final String OPTION_MACA = "maca";
	public static final String OPTION_NERD = "nerd";
	public static final String OPTION_OUTPUT_FILE = "t";
	public static final String OPTION_OUTPUT_FORMAT = "o";
	public static final String OPTION_PYTHON = "python";
	public static final String OPTION_SILENT = "silent";
	public static final String OPTION_TEMPLATE = "template";
	public static final String OPTION_USE = "use";
	public static final String OPTION_VERBOSE = "verbose";
	public static final String OPTION_VERBOSE_DETAILS = "verboseDetails";
	public static final String OPTION_WMBT = "wmbt";
	
	
	// List of argument read from cmd
	public String mode = "";

	// List of options read from cmd
	public boolean nested = false;
	public boolean useHeuristics = false;
	public boolean verbose = false;
	public boolean verboseDetails = false;
	public boolean silent = false;

	public ArrayList<Filter> filters = new ArrayList<Filter>();
	public ArrayList<String> features = new ArrayList<String>();
	public ArrayList<String> featureNames = new ArrayList<String>();
//	public DictionaryManager dm = new DictionaryManager();
	public String arg1 = null;
	public String arg2 = null;
	public String arg3 = null;
//	public String nerd = null;		// replaced with e.g. getOption(OPTION_NERD)
//	public String inputFile = "";
//	public String outputFile = "";
//	public String python = "python";
	public ArrayList<Integer> folds = new ArrayList<Integer>();
	public ArrayList<String> chunkersDescription = new ArrayList<String>();
	public ArrayList<String> corpusDescriptions = new ArrayList<String>();
	public TreeSet<String> common = new TreeSet<String>();
	
	/**
	 * Constructor
	 */
	public LinerOptions(){
		this.options = makeOptions();
		this.properties = new Properties();
		
		/* Ustawienie domyślnych parametrów. */
		this.properties.setProperty(LinerOptions.OPTION_OUTPUT_FORMAT, "ccl");
		this.properties.setProperty(LinerOptions.OPTION_INPUT_FORMAT, "ccl");
	}
	
	public static boolean isOption(String name){
		return LinerOptions.get().getProperties().containsKey(name);
	}
	
	/**
	 * 
	 * @return
	 */
	public Options getApacheOptions(){
		return this.options;
	}
	
	public Properties getProperties() {
		return this.properties;
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void parse(String[] args) throws Exception{
		
		// Use to gather confugiration description
		StringBuilder configDesc = new StringBuilder();
		
		// Try to load configuration from local.ini
    	if (new File(LOCAL_INI).exists()) {
    		try {
    			this.parseFromIni(LOCAL_INI, configDesc);
    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    	}
	
    	// Parse parameters passed by command line
		CommandLine line = new GnuParser().parse(options, args);
		
		if (line.hasOption(OPTION_HELP)) {
			printHelp();
			System.exit(0);
		}

    	if (this.mode == null && line.getArgs().length == 0)
    		throw new ParameterException("mode not set");
    	
    	if ( line.getArgs().length == 0){
    		throw new UnrecognizedOptionException("Mode name not given");
    	}
    	
    	this.mode = line.getArgs()[0];
    	configDesc.append("> Mode: " + this.mode + "\n");

    	this.parseParameters(line, configDesc);
    	    	
		if (this.features.size() > 0)
			FeatureGenerator.initialize();
		
		for (String cd : this.corpusDescriptions)
			CorpusFactory.get().parse(cd);
    	
		this.configurationDescription = configDesc.toString();
	}
    
    /**
     * Read configuration from an ini file.
     * @param filename
     * @throws Exception 
     */
    private void parseFromIni(String filename, StringBuilder configDesc)
    	throws Exception {
    	File iniFile = new File(filename); 
        BufferedReader br = new BufferedReader(new FileReader(iniFile));
            
        StringBuffer sb = new StringBuffer();
        String eachLine = br.readLine().trim();
            
        while(eachLine != null) {
        	if ((!eachLine.startsWith("#")) &&
        		(!eachLine.isEmpty()))
        		sb.append(eachLine + " ");
           	eachLine = br.readLine();
		}

        // Insert fixed paths
        String parameters = sb.toString().trim();
        // TODO ścieżka bezwzględna do pliku
//        String iniPath = "./";
//        if (iniFile.getParentFile() != null)
//        	iniPath = iniFile.getParentFile().getAbsolutePath();
		String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        parameters = parameters.replace("{INI_PATH}", iniPath);
            
      	CommandLine line = new GnuParser().parse(makeOptions(), parameters.split(" "));        		
        configDesc.append("> Load parameters from a ini file: " + filename + "\n");

        this.parseParameters(line, configDesc);
    }
	
	/**
	 * Adds options from a given CommandLine to own properties.
	 * @param line
	 * @throws Exception 
	 * @throws Exception 
	 */
	private void parseParameters(CommandLine line, StringBuilder configDesc) throws Exception {
		
		// Copy parameters passed by command line to properties
		Iterator<?> i_options = line.iterator();
		while (i_options.hasNext()) {
			Option o = (Option)i_options.next();
			if (o.getValue() == null)	// don't take boolean parameters
				continue;
			if ( o.getOpt().equals("ini") ){
				configDesc.append( String.format(PARAM_PRINT, o.getOpt(), o.getValue() + "\n" ) );
    			this.parseFromIni(o.getValue(), configDesc);
			}				
			else if (o.getLongOpt() == null) {
				this.properties.setProperty(o.getOpt(), o.getValue());
				configDesc.append( String.format(PARAM_PRINT, o.getOpt(), o.getValue() + "\n" ) );
			}
			else {
				this.properties.setProperty(o.getLongOpt(), o.getValue());
				configDesc.append( String.format(PARAM_PRINT, o.getLongOpt(), o.getValue() + "\n" ) );
			}
		}
		
		// sets boolean fields
		if (line.hasOption(OPTION_VERBOSE))
			this.verbose = true;
		if (line.hasOption(OPTION_VERBOSE_DETAILS))
			this.verboseDetails = true;
		
		// read feature definitions and initialize feature generator
		if (line.hasOption(OPTION_FEATURE)) {
			for (String feature : line.getOptionValues(OPTION_FEATURE)) {
				this.features.add(feature);
				int pos = feature.indexOf(":");
				this.featureNames.add((pos > -1) ? feature.substring(0, pos) : feature);
			}
		}
		
		// read chunker descriptions
		if (line.hasOption(OPTION_CHUNKER)) {
			for (String cd : line.getOptionValues(OPTION_CHUNKER))
				this.chunkersDescription.add(cd);
		}
		
		// read corpus descriptions
		if (line.hasOption(OPTION_CORPUS)) {
			for (String cd : line.getOptionValues(OPTION_CORPUS)) {
				this.corpusDescriptions.add(cd);
			}
		}
		
		// filters
		if (line.hasOption(OPTION_FILTER))
			parseFilter(line.getOptionValue(OPTION_FILTER));
		
		if (line.hasOption(OPTION_PYTHON))
			this.properties.setProperty(OPTION_PYTHON, line.getOptionValue(OPTION_PYTHON));
		else
			this.properties.setProperty(OPTION_PYTHON, "python");
		
		// read template descriptions
		if (line.hasOption(OPTION_TEMPLATE)) {
			for (String td : line.getOptionValues(OPTION_TEMPLATE)) {
				TemplateFactory.get().parse(td);
			}
		}
		
		// Arguments
		if (line.getArgs().length > 1 && line.getArgs()[1].length() > 0 ){
			this.arg1 = line.getArgs()[1];
			configDesc.append( String.format(PARAM_PRINT, "Argument 1", this.arg1) + "\n" );
		}
		
		if (line.getArgs().length > 2 && line.getArgs()[2].length() > 0 ){
			this.arg2 = line.getArgs()[2];
			configDesc.append( String.format(PARAM_PRINT, "Argument 2", this.arg2) + "\n" );
		}
		
		if (line.getArgs().length > 3 && line.getArgs()[3].length() > 0 ){
			this.arg3 = line.getArgs()[3];
			configDesc.append( String.format(PARAM_PRINT, "Argument 3", this.arg3) + "\n" );
		}
	}
		
	/**
	 * 
	 * @param filter
	 * @throws Exception
	 */
	public void parseFilter(String filter) throws ParameterException{
		String filters[] = filter.split(",");
		for (String f : filters){
			boolean found = false;
			if (f.equals("uppercase") || f.equals("all")) {
				this.filters.add(new FilterUppercase());
				found = true;
			}
//			if ( found |= (f.equals("trim") || f.equals("all")) )
//				this.filters.add(new FilterTrim());
			if (f.equals("firstnotlower") || f.equals("all")) {
				this.filters.add(new FilterFirstNotLower());
				found = true;
			}
			if (f.equals("hasvowel") || f.equals("all")) {
				this.filters.add(new FilterHasVowel());
				found = true;
			}
			if (f.equals("nosymbol") || f.equals("all")) {
				this.filters.add(new FilterNoSymbol());
				found = true;
			}
			if (f.equals("beforesie") || f.equals("all")) {
				this.filters.add(new FilterBeforeSie());
				found = true;
			}
			if (f.equals("nounderline") || f.equals("all")) {
				this.filters.add(new FilterNoUnderline());
				found = true;
			}
			if (f.equals("nodot") || f.equals("all")) {
				this.filters.add(new FilterNoDot());
				found = true;
			}
			if (f.equals("nohyphen") || f.equals("all")) {
				this.filters.add(new FilterNoHyphen());
				found = true;
			}
			if (f.equals("patternulu") || f.equals("all")) {
				this.filters.add(new FilterPatternULU());
				found = true;
			}
//			if (f.equals("length") || f.equals("all")) {
//				this.filters.add(new FilterLength());
//				found = true;
//			}
			if (f.equals("hasalphanumeric") || f.equals("all")) {
				this.filters.add(new FilterHasAlphanumeric());
				found = true;
			}
//			if ( found |= (f.equals("extendtoword") || f.equals("all")) )
//				this.filters.add(new FilterExtendToWord());
			if (f.equals("cutroadprefix") || f.equals("all")) {
				this.filters.add(new FilterCutRoadPrefix());
				found = true;
			}
			
			if (!found)
				throw new ParameterException("Unknown filter '"+f+"'");
		}
	}
	
	
	private Options makeOptions(){
    	Options options = new Options();    	

    	options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("load chunker from chunker factory")
				.create(OPTION_CHUNKER));
    	options.addOption(OptionBuilder.withArgName("filename").hasArg()
				.withDescription("loads a list of common words from a file")
				.create("common"));
		options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("load a specified file as a corpus")
				.create(OPTION_CORPUS));
    	options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("feature name recognized by NERD (name or name:dictionary_file)")
				.create(OPTION_FEATURE));
    	options.addOption(OptionBuilder.withArgName("filters").hasArg()
				.withDescription("filters to apply")
				.create("filter"));
    	options.addOption(OptionBuilder.withArgName("span").hasArg()
				.withDescription("number of folds")
				.create("fold"));
    	options.addOption(OptionBuilder.withArgName("gazetters").hasArg()
				.withDescription("(multiple) loads a gazetteer: 'TYPE:location'")
				.create("gaze"));
    	options.addOption(OptionBuilder.withArgName("file name").hasArg()
				.withDescription("name of file with a list of gazetteers. File line format: TYPE:location")
				.create("gazef"));
		options.addOption(OptionBuilder.withLongOpt(OPTION_HELP).withDescription("print this help")
				.create(OPTION_HELP));
    	options.addOption(OptionBuilder
				.withArgName("filename").hasArg().withDescription("name of file with configuration")
				.create(OPTION_INI));
		options.addOption(OptionBuilder.withArgName("filename").hasArg()
			.withDescription("read input from file")
			.create(OPTION_INPUT_FILE));
		options.addOption(OptionBuilder.withArgName("format").hasArg()
			.withDescription("input format (iob or ccl)")
			.create(OPTION_INPUT_FORMAT));
		options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("path to maca (for batch mode)")
				.create(OPTION_MACA));
    	options.addOption(OptionBuilder.withArgName("filename").hasArg()
				.withDescription("path to location of nerd.py")
				.create(OPTION_NERD));
    	options.addOption(OptionBuilder.withArgName("filename").hasArg()
				.withDescription("path to location of python interpreter")
				.create(OPTION_PYTHON));
		options.addOption(OptionBuilder.withArgName("filename").hasArg()
			.withDescription("save output to file")
			.create(OPTION_OUTPUT_FILE));
		options.addOption(OptionBuilder.withArgName("chunkers").hasArg()
			.withDescription("specify chunkers to use")
			.create(OPTION_USE));
		options.addOption(OptionBuilder.withArgName("format").hasArg()
				.withDescription("output format (iob or ccl)")
				.create(OPTION_OUTPUT_FORMAT));
		options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("define feature template")
				.create(OPTION_TEMPLATE));
		options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("path to WMBT (for batch mode)")
				.create(OPTION_WMBT));
    	options.addOption(new Option(OPTION_SILENT, false, "does not print any additional text in batch mode"));
    	options.addOption(new Option(OPTION_VERBOSE, false, "print brief information about processing"));
    	options.addOption(new Option(OPTION_VERBOSE_DETAILS, false, "print detailed information about processing"));
    	return options;		
	}
	
    /**
     * Prints program usage and description.
     * @param options
     */
    public void printHelp(){
    	System.out.println("--------------------------------------------------*");
    	System.out.println("* A tool for Named Entity Recognition for Polish. *");
    	System.out.println("*     Authors: Michał Marcińczuk (2010–2011)      *");
    	System.out.println("*              Maciej Janicki (2011)              *");
    	System.out.println("* Institution: Wrocław University of Technology.  *");
    	System.out.println("--------------------------------------------------*");
    	System.out.println();
		new HelpFormatter().printHelp("java -jar liner.jar <mode> [options]", options);
		System.out.println();
    	System.out.println("Modes:");
    	System.out.println("  batch               - ner batch moge");
    	System.out.println("                        Parameters: -feature, -chunker, -nerd");
    	System.out.println("  convert             - convert text from one format to another");
    	System.out.println("                        Parameteres: -i, -o, -f, -t");
    	//System.out.println("  dicts <arg1>        - print dictionary statistics");
    	System.out.println("  eval                - evaluate chunker on given input");
    	System.out.println("                        Parameters: -i, -f, -feature, -chunker, -nerd");
    	System.out.println("  evalcv              - perform 10-fold cross validation");
    	System.out.println("                        Parameters: -i, -f, -feature, -chunker, -nerd");
    	System.out.println("  pipe                - annotate data");
    	System.out.println("                        Parameters: -i, (-f), -o, (-t), -feature, -chunker, -nerd");
    	System.out.println("  train               - train CRFPP chunker");
    	System.out.println("                        Parameters: -feature, -chunker, -template, -nerd");
    	//System.out.println("  tag <text>          - tag `text` using chunker specified by `--chuner` parameters");
    	System.out.println("");
    	//System.out.println("Filters:");
    	//this.printFilters();
    	System.out.println("");
    	System.out.println("Chunker factory (patterns for `-chunker` parameter):");
    	System.out.println(ChunkerFactory.getDescription());
    }	

	public void printConfigurationDescription(){
		Main.log(this.configurationDescription);
	}
    
	/**
	 * Print list of activated filters.
	 */
	public void printFilters(){
		Main.log(">> Filters:");
//		int i=1;
//		for (Filter filter : this.filters)
//			Main.log("  "+(i++)+") "+filter.getDescription());
	}

}
