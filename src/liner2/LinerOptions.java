package liner2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import liner2.chunker.factory.ChunkerFactory;
import liner2.tools.CorpusFactory;
import liner2.tools.TemplateFactory;

//import liner.chunker.HeuristicChunker;
//import liner.chunker.factory.ChunkerFactory;
//import liner.filter.Filter;
//import liner.filter.FilterBeforeSie;
//import liner.filter.FilterCutRoadPrefix;
//import liner.filter.FilterExtendToWord;
//import liner.filter.FilterFirstNotLower;
//import liner.filter.FilterHasAlphanumeric;
//import liner.filter.FilterHasVowel;
//import liner.filter.FilterLength;
//import liner.filter.FilterNoDot;
//import liner.filter.FilterNoHyphen;
//import liner.filter.FilterNoSymbol;
//import liner.filter.FilterNoUnderline;
//import liner.filter.FilterPatternULU;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

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
        
	private Options options = null;
	private String configurationDescription = "";
	private Properties properties;
	
	private static final String LOCAL_INI = "local.ini";
	
	public static final String OPTION_CHUNKER = "chunker";
	public static final String OPTION_COMMON = "common";
	public static final String OPTION_CORPUS = "corpus";
	public static final String OPTION_FEATURE = "feature";
	public static final String OPTION_FILTER = "filter";
	public static final String OPTION_HEURISTICS = "heuristics";
	public static final String OPTION_INI = "ini";
	public static final String OPTION_INPUT_FILE = "f";
	public static final String OPTION_INPUT_FORMAT = "i";
	public static final String OPTION_NERD = "nerd";
	public static final String OPTION_OUTPUT_FILE = "t";
	public static final String OPTION_OUTPUT_FORMAT = "o";
	public static final String OPTION_PYTHON = "python";
	public static final String OPTION_SILENT = "silent";
	public static final String OPTION_TEMPLATE = "template";
	public static final String OPTION_USE = "use";
	public static final String OPTION_VERBOSE = "verbose";
	public static final String OPTION_VERBOSE_DETAILS = "verboseDetails";
	
	
	// List of argument read from cmd
	public String mode = "";

	// List of options read from cmd
	public boolean nested = false;
	public boolean useHeuristics = false;
	public boolean verbose = false;
	public boolean verboseDetails = false;
	public boolean silent = false;

//	public ArrayList<Filter> filters = new ArrayList<Filter>();
	public ArrayList<Double> weights = new ArrayList<Double>();
	public ArrayList<String> summaryTypesOrder = new ArrayList<String>();
	public ArrayList<String> features = new ArrayList<String>();
//	public DictionaryManager dm = new DictionaryManager();
	public String arg1 = null;
	public String arg2 = null;
	public String arg3 = null;
//	public String nerd = null;		// replaced with e.g. getOption(OPTION_NERD)
//	public String inputFile = "";
//	public String outputFile = "";
//	public String inputFormat = "ccl";
//	public String outputFormat = "ccl";
//	public String python = "python";
	public ArrayList<Integer> folds = new ArrayList<Integer>();
	public ArrayList<String> chunkersDescription = new ArrayList<String>();
	public TreeSet<String> common = new TreeSet<String>();
	
	/**
	 * Constructor
	 */
	public LinerOptions(){
		this.options = makeOptions();
		this.properties = new Properties();
	}
	
	public static boolean isOption(String name){
		return LinerOptions.get().getProperties().contains(OPTION_VERBOSE);
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

    	if (this.mode == null && line.getArgs().length == 0)
    		throw new ParseException("mode not set");
    	
    	this.mode = line.getArgs()[0];
    	configDesc.append("> Mode: " + this.mode + "\n");

    	this.parseParameters(line, configDesc);
    	
    	// If the ini parameter is set then load configuration form a file
    	if (properties.containsKey("ini")){
    		String ini = properties.getProperty("ini");
    		try {
    			this.parseFromIni(ini, configDesc);
    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    	}
    	    	
		this.configurationDescription = configDesc.toString();
		this.processParameters();   	
	}
    
    /**
     * Read configuration from an ini file.
     * @param filename
     */
    private void parseFromIni(String filename, StringBuilder configDesc)
    	throws IOException, FileNotFoundException, ParseException {
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
        String iniPath = "./";
        if (iniFile.getParentFile() != null)
        	iniPath = iniFile.getParentFile().getAbsolutePath();
        parameters = parameters.replace("{INI_PATH}", iniPath);
            
      	CommandLine line = new GnuParser().parse(makeOptions(), parameters.split(" "));        		
        configDesc.append("> Load parameters from a ini file: " + filename + "\n");

        this.parseParameters(line, configDesc);
    }
	
	/**
	 * Adds options from a given CommandLine to own properties.
	 * @param line
	 * @throws Exception 
	 */
	private void parseParameters(CommandLine line, StringBuilder configDesc) {
		
		// Copy parameters passed by command line to properties
		Iterator<?> i_options = line.iterator();
		while (i_options.hasNext()) {
			Option o = (Option)i_options.next();
			if (o.getValue() == null)	// don't take boolean parameters
				continue;
			if (o.getLongOpt() == null) {
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
		
		// read chunker descriptions
		if (line.hasOption(OPTION_CHUNKER)) {
			for (String cd : line.getOptionValues(OPTION_CHUNKER))
				this.chunkersDescription.add(cd);
		}
		
		// read corpus descriptions
		if (line.hasOption(OPTION_CORPUS)) {
			for (String cd : line.getOptionValues(OPTION_CORPUS)) {
				try {
					CorpusFactory.get().parse(cd);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		// read template descriptions
		if (line.hasOption(OPTION_TEMPLATE)) {
			for (String td : line.getOptionValues(OPTION_TEMPLATE)) {
				try {
					TemplateFactory.get().parse(td);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
	
	/*
	 * Tutaj dodatkowe przetwarzanie parametrów, korzystając z pola
	 * properties.
	 * configDesc jest już zrobione i nie występuje w tej metodzie!
	 */
	private void processParameters() throws Exception {

		// Parameters
/*		if ( line.hasOption(LinerOptions.OPTION_CHUNKER)){
			String[] values = line.getOptionValues(LinerOptions.OPTION_CHUNKER);
			//for (String chunkerDescription : values){
//				if (!ChunkerFactory.get().parse(chunkerDescription))
//					throw new Exception(String.format("Unknown chunker description '%s'", chunkerDescription));
//				else
//					this.chunkersDescription.add(chunkerDescription);
				//configDesc.append( String.format(PARAM_PRINT, OPTION_CHUNKER, chunkerDescription) + "\n" );
			//}
		}

/*		if ( line.hasOption(OPTION_COMMON) ){
			this.common = new TreeSet<String>();
			for (String filename : line.getOptionValues(OPTION_COMMON)){
//				this.common.addAll( DictionaryManager.loadList(filename) );
				configDesc.append( String.format(PARAM_PRINT, OPTION_COMMON, filename) + "\n" );
			}
		}

		if (line.hasOption("dictNested"))
		{
			configDesc.append( String.format(PARAM_PRINT, "dictNested", true) + "\n" );
			this.nested = true;
		}

		if (line.hasOption(OPTION_DICT_UNAMBIGUOUS)){
//			LinerOptions.get().dm.removeAmbiguous();
			configDesc.append( String.format(PARAM_PRINT, OPTION_DICT_UNAMBIGUOUS, true) + "\n" );
		}
		
		if (line.hasOption(OPTION_FEATURE)){
			for ( String feature : line.getOptionValues(OPTION_FEATURE) ){
				this.features.add(feature);
				configDesc.append( String.format(PARAM_PRINT, OPTION_FEATURE, feature) + "\n" );
			}
		}
		
		if (line.hasOption(OPTION_FILE)){
			this.inputFile = line.getOptionValue(OPTION_FILE);
			configDesc.append( String.format(PARAM_PRINT, OPTION_FILE, this.inputFile + "\n" ) );
		}
		
		if (line.hasOption(OPTION_FILTER)){
			configDesc.append( String.format(LinerOptions.PARAM_PRINT, line.getOptionValue(OPTION_FILTER), true) + "\n");
			this.parseFilter(line.getOptionValue(OPTION_FILTER));
		}

		if (line.hasOption("fold")){
			configDesc.append( String.format(LinerOptions.PARAM_PRINT, "fold", line.getOptionValue("fold")) + "\n" );
			for (String part : line.getOptionValue("fold").split(","))
			{
				if (part.contains("-"))
				{
					String[] span = part.split("-");
					int a = Integer.parseInt(span[0]);
					int b = Integer.parseInt(span[1]);
					for ( ; a<=b ; a++ )
						this.folds.add(a);
				}
				else
					this.folds.add(Integer.parseInt(part));
			}
		}else{
			if (this.folds.size() == 0)
				for (int i=1; i<=10; i++)
					this.folds.add(i);
		}

		if ( line.hasOption("gazef"))
		{
            BufferedReader br = new BufferedReader(new FileReader(line.getOptionValue("gazef")));			
            String dictline = null;
            while ( (dictline=br.readLine()) != null ){
            	if (dictline.contains(":") && !dictline.startsWith("#")) {
	            	String[] parts = dictline.trim().split(":");
//            		this.dm.load(parts[0], parts[1]);
            	}
            }
            br.close();
		}
		
		if (line.hasOption("gaze")){
			for (String option : line.getOptionValues("gaze")){
				String[] parts = option.split(":");
//        		this.dm.load(parts[0], parts[1]);
			}
		}
		    	
		if ( line.hasOption(LinerOptions.OPTION_HEURISTICS) ){
			this.useHeuristics = true;		
//			HeuristicChunker.chunker = new HeuristicChunker();
			configDesc.append( String.format(LinerOptions.PARAM_PRINT, LinerOptions.OPTION_HEURISTICS, true) + "\n");
		}
		
		if ( line.hasOption(OPTION_INPUT_FORMAT) ){
			this.inputFormat = line.getOptionValue(OPTION_INPUT_FORMAT);
			configDesc.append( String.format(PARAM_PRINT, OPTION_INPUT_FORMAT, this.inputFormat + "\n" ) );
		}

		if (line.hasOption("summaryTypesOrder"))
		{
			this.summaryTypesOrder.addAll(Arrays.asList(line.getOptionValue("summaryTypesOrder").toString().split(",")));
			configDesc.append( String.format(LinerOptions.PARAM_PRINT, "summaryTypesOrder", line.getOptionValue("summaryTypesOrder")) + "\n" );
		}

		if ( line.hasOption(OPTION_NERD) ){
			this.nerd = line.getOptionValue(OPTION_NERD);
			configDesc.append( String.format(PARAM_PRINT, OPTION_NERD, this.nerd + "\n" ) );
		}
		
		if ( line.hasOption(OPTION_OUTPUT_FORMAT) ){
			this.outputFormat = line.getOptionValue(OPTION_OUTPUT_FORMAT);
			configDesc.append( String.format(PARAM_PRINT, OPTION_OUTPUT_FORMAT, this.outputFormat + "\n" ) );
		}

		if ( line.hasOption(OPTION_PYTHON) ){
			this.python = line.getOptionValue(OPTION_PYTHON);
			configDesc.append( String.format(PARAM_PRINT, OPTION_PYTHON, this.nerd + "\n" ) );
		}

		if ( line.hasOption(LinerOptions.OPTION_SILENT)){
			this.silent = true;
			configDesc.append( String.format(LinerOptions.PARAM_PRINT, LinerOptions.OPTION_SILENT, true) + "\n" );			
		}
		
		if (line.hasOption(OPTION_TARGET)){
			this.outputFile = line.getOptionValue(OPTION_TARGET);
			configDesc.append( String.format(PARAM_PRINT, OPTION_TARGET, this.outputFile + "\n" ) );
		}

		if ( line.hasOption(LinerOptions.OPTION_WEIGHTS) ){
			for ( String part : line.getOptionValue(LinerOptions.OPTION_WEIGHTS).split(","))
				this.weights.add(Double.parseDouble(part));
			configDesc.append( String.format(LinerOptions.PARAM_PRINT, LinerOptions.OPTION_WEIGHTS, line.getOptionValue(LinerOptions.OPTION_WEIGHTS)) + "\n");
		}

		// Instructions after parsing arguments and paramenters
//		LinerOptions.get().dm.getEntries().remove("COMMON");		
*/
	}
	
	/**
	 * 
	 * @param filter
	 * @throws Exception
	 */
	public void parseFilter(String filter) throws Exception{
		String filters[] = filter.split(",");
		for (String f : filters){
			boolean found = false;
//			if ( found |= (f.equals("uppercase") || f.equals("all")) )
//				this.filters.add(new FilterUppercase());
//			if ( found |= (f.equals("trim") || f.equals("all")) )
//				this.filters.add(new FilterTrim());
//			if ( found |= (f.equals("firstnotlower") || f.equals("all")) )
//				this.filters.add(new FilterFirstNotLower());
//			if ( found |= (f.equals("hasvowel") || f.equals("all")) )
//				this.filters.add(new FilterHasVowel());
//			if ( found |= (f.equals("nosymbol") || f.equals("all")) )
//				this.filters.add(new FilterNoSymbol());
//			if ( found |= (f.equals("beforesie") || f.equals("all")) )
//				this.filters.add(new FilterBeforeSie());
//			if ( found |= (f.equals("nounderline") || f.equals("all")) )
//				this.filters.add(new FilterNoUnderline());
//			if ( found |= (f.equals("nodot") || f.equals("all")) )
//				this.filters.add(new FilterNoDot());
//			if ( found |= (f.equals("nohyphen") || f.equals("all")) )
//				this.filters.add(new FilterNoHyphen());
//			if ( found |= (f.equals("patternulu") || f.equals("all")) )
//				this.filters.add(new FilterPatternULU());
//			if ( found |= (f.equals("length") || f.equals("all")) )
//				this.filters.add(new FilterLength());
//			if ( found |= (f.equals("hasalphanumeric") || f.equals("all")) )
//				this.filters.add(new FilterHasAlphanumeric());
//			if ( found |= (f.equals("extendtoword") || f.equals("all")) )
//				this.filters.add(new FilterExtendToWord());
//			if ( found |= (f.equals("cutroadprefix") || f.equals("all")) )
//				this.filters.add(new FilterCutRoadPrefix());
			
			if (!found)
				throw new UnrecognizedOptionException("Unknown filter '"+f+"'");
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
    	options.addOption(OptionBuilder
				.withArgName("filename").hasArg().withDescription("name of file with configuration")
				.create(OPTION_INI));
		options.addOption(OptionBuilder.withArgName("filename").hasArg()
			.withDescription("read input from file")
			.create(OPTION_INPUT_FILE));
		options.addOption(OptionBuilder.withArgName("format").hasArg()
			.withDescription("input format (iob or ccl)")
			.create(OPTION_INPUT_FORMAT));
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
    	System.out.println("  batch <arg1>        - ner batch moge");
    	System.out.println("                        <arg1> -- name of file with ner model");
    	System.out.println("  convert             - convert text from one format to another");
    	System.out.println("                        Parameteres: -i, -o, -f, -t");
    	System.out.println("  dicts <arg1>        - print dictionary statistics");
    	System.out.println("  eval <arg1> <arg2>  - train on arg1 file and test on arg2 file");
    	System.out.println("  evalcv <arg1>       - perform 10-fold cross validation");
    	System.out.println("  pipe                - read xces data from standard input and annotate it");
    	System.out.println("  tag <text>          - tag `text` using chunker specified by `--chuner` parameters");
    	System.out.println("");
    	//System.out.println("Filters:");
    	//this.printFilters();
    	System.out.println("");
    	System.out.println("Chunker factory (patterns for `-chunker` parameter):");
    	System.out.println(ChunkerFactory.get().getDescription());
    }	

	public void printConfigurationDescription(){
		Main.log(this.configurationDescription);
	}
    
	/**
	 * Print list of activated filters.
	 */
	public void printFilters(){
		Main.log(">> Filters:");
		int i=1;
//		for (Filter filter : this.filters)
//			Main.log("  "+(i++)+") "+filter.getDescription());
	}

}
