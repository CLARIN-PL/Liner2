package liner2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Properties;

import liner2.chunker.factory.ChunkerFactory;
import liner2.reader.AbstractDocumentReader;
import liner2.reader.ReaderFactory;
import liner2.tools.ParameterException;
import liner2.tools.Template;
import liner2.tools.TemplateFactory;
import liner2.writer.AbstractDocumentWriter;
import liner2.writer.CclBatchWriter;
import liner2.writer.WriterFactory;

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
	public static LinerOptions getGlobal(){
		return linerOptions;
	}
	
	/**
	 * Get property value
	 */
	public String getOption(String option) {
		return properties.getProperty(option);
	}

    public String getOptionUse() {
        return properties.getProperty(OPTION_USE);
    }

	private Options options = null;
	private String configurationDescription = "";
	private Properties properties;
	
	private static final String LOCAL_INI = "local.ini";
	
	public static final String OPTION_CHUNKER = "chunker";
	public static final String OPTION_COMMON = "common";
	public static final String OPTION_DB_HOST = "db_host";
	public static final String OPTION_DB_NAME = "db_name";
	public static final String OPTION_DB_PASSWORD = "db_pass";
	public static final String OPTION_DB_PORT = "db_port";
	public static final String OPTION_DB_USER = "db_user";
	public static final String OPTION_DB_URI = "db_uri";
	public static final String OPTION_FEATURE = "feature";
	public static final String OPTION_FOLDS_NUMBER = "folds_number";
	public static final String OPTION_HELP = "help";
	public static final String OPTION_HEURISTICS = "heuristics";
	public static final String OPTION_INI = "ini";
	public static final String OPTION_IS = "is";
	public static final String OPTION_INPUT_FILE = "f";
	public static final String OPTION_INPUT_FORMAT = "i";
	public static final String OPTION_IP = "ip";
	public static final String OPTION_MAX_THREADS = "max_threads";
	public static final String OPTION_OUTPUT_FILE = "t";
	public static final String OPTION_OUTPUT_FORMAT = "o";
	public static final String OPTION_PORT = "p";
	public static final String OPTION_SILENT = "silent";
	public static final String OPTION_TEMPLATE = "template";
	public static final String OPTION_TYPES = "types";
	public static final String OPTION_USE = "use";
    public static final String OPTION_ARFF_TEMPLATE = "arff-template";
	public static final String OPTION_VERBOSE = "verbose";
	public static final String OPTION_VERBOSE_DETAILS = "verboseDetails";
		
	// List of argument read from cmd
	public String mode = "";

	// List of options read from cmd
	public boolean verbose = false;
	public boolean verboseDetails = false;
	public boolean silent = false;

    public LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();
    public HashMap<String, Template> templates = new HashMap<String, Template>();
    Template arffTemplate;
	public String arg1 = null;
	public String arg2 = null;
	public String arg3 = null;
	public String linerPath = "";
	public LinkedHashSet<String> chunkersDescriptions = new LinkedHashSet<String>();
	private String cvTrainData = null; 
	private HashSet<String> types = null;
	 
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
		return LinerOptions.getGlobal().getProperties().containsKey(name);		
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

    public void loadIni(String ini){
        try {
            parseFromIni(ini, new StringBuilder(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFeatures(String featuresIni){
        try {
            features.clear();
            parseFromIni(featuresIni, new StringBuilder(), new ArrayList<String>(Arrays.asList("feature", "template", "ini")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadChunkerDescription(String chunkerIni){
        try {
            parseFromIni(chunkerIni, new StringBuilder(), new ArrayList<String>(Arrays.asList("chunker", "use")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Template getTemplate(String name) throws Exception {
        Template t = templates.get(name);
        if (t != null){
            return templates.get(name);
        }
        else{
            throw new Exception("Invalid template name: "+name);
        }
    }

    public Template getArffTemplate() throws Exception {
         Object templateName = properties.get(OPTION_ARFF_TEMPLATE);
        if (templateName != null){
            return getTemplate(String.valueOf(templateName));
        }
        else{
            if (templates.isEmpty()) {
                throw new Exception("No templates specified in config");
            }
            else {
                return templates.values().iterator().next();
            }
        }
    }
    
    public int getFoldsNumber(){
    	if (this.properties.containsKey(LinerOptions.OPTION_FOLDS_NUMBER))
    		return Integer.parseInt(this.properties.getProperty(LinerOptions.OPTION_FOLDS_NUMBER));
    	else
    		return 10;
    			
    }
    
    public HashSet<String> getTypes(){
    	if ( this.types == null )
	    	if ( isOption(OPTION_TYPES) ){
	    		HashSet<String> types = new HashSet<String>();
	    		for (String type : getOption(OPTION_TYPES).split(","))
	    			types.add(type);
	    		this.types = types;
	    	}
	    	else
	    		this.types = new HashSet<String>();
    	return this.types;
    }
    
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void parse(String[] args) throws Exception{
		
		// Use to gather confugiration description
		StringBuilder configDesc = new StringBuilder();
		
		String path = getClass().getResource("").getPath();
		path = path.substring(path.indexOf(':')+1, path.indexOf('!'));
		path = path.substring(0, path.lastIndexOf('/')+1);
		this.linerPath = path;

		// Try to load configuration from local.ini
    	if (new File(this.linerPath + LOCAL_INI).exists()) {
    		try {
    			this.parseFromIni(this.linerPath + LOCAL_INI, configDesc, null);
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

    	this.parseParameters(line, configDesc, null);
		
		this.configurationDescription = configDesc.toString();
	}
    
    /**
     * Read configuration from an ini file.
     * @param filename
     * @throws Exception 
     */
    private void parseFromIni(String filename, StringBuilder configDesc, ArrayList<String> allowedOptions)
    	throws Exception {
    	File iniFile = new File(filename); 
        BufferedReader br = new BufferedReader(new FileReader(iniFile));
            
        StringBuffer sb = new StringBuffer();
        String eachLine = br.readLine();
            
        while(eachLine != null) {
        	eachLine = eachLine.trim();
        	if ((!eachLine.startsWith("#")) && (!eachLine.isEmpty())){
                boolean validOption = false;
                if( allowedOptions != null ){
                    for(String option: allowedOptions)
                        if(eachLine.startsWith("-"+option+" ") || eachLine.equals("-"+option)){
                            validOption = true;
                            break;
                        }
                }
                else
                    validOption = true;
                if(!validOption){
                	br.close();
                    throw new Exception("IniParseError in: " + filename + ". Not allowed option: " + eachLine);
                }
        		sb.append(eachLine + " ");
            }
           	eachLine = br.readLine();
		}
        br.close();

        // Insert fixed paths
        String parameters = sb.toString().trim();
		String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        parameters = parameters.replace("{INI_PATH}", iniPath);
            
      	CommandLine line = new GnuParser().parse(makeOptions(), parameters.split(" "));
        configDesc.append("> Load parameters from a ini file: " + filename + "\n");

        this.parseParameters(line, configDesc, allowedOptions);
    }
	
	/**
	 * Adds options from a given CommandLine to own properties.
	 * @param line
	 * @throws Exception 
	 * @throws Exception 
	 */
	private void parseParameters(CommandLine line, StringBuilder configDesc, ArrayList<String> allowedOptions) throws Exception {
		
		// Copy parameters passed by command line to properties
		Iterator<?> i_options = line.iterator();
		while (i_options.hasNext()) {
			Option o = (Option)i_options.next();
			if (o.getValue() == null)	// don't take boolean parameters
				continue;
			if ( o.getOpt().equals("ini") ){
				configDesc.append( String.format(PARAM_PRINT, o.getOpt(), o.getValue() + "\n" ) );
    				this.parseFromIni(o.getValue(), configDesc, allowedOptions);
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
		if (line.hasOption(OPTION_SILENT))
			this.silent = true;
		
		// read feature definitions and initialize feature generator
		if (line.hasOption(OPTION_FEATURE)) {
			for (String feature : line.getOptionValues(OPTION_FEATURE)) {
				String[] splitted = feature.split(":");
                String featureName;
                if(splitted.length > 2 && splitted[1].length() == 1) {
                    featureName = splitted[0]+splitted[1];
                }
				else {
                    featureName = splitted[0];
                }
                this.features.put(featureName, feature);
            }
		}
		
		// read chunker descriptions
		if (line.hasOption(OPTION_CHUNKER)) {
			for (String cd : line.getOptionValues(OPTION_CHUNKER)){
					this.chunkersDescriptions.add(cd);
            }
		}

        // read template descriptions
        if (line.hasOption(OPTION_TEMPLATE)) {
            for (String td : line.getOptionValues(OPTION_TEMPLATE)) {
                TemplateFactory.parseFeature(td, templates, features.keySet());
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
			
	public void setCvTrain(String data){
		this.cvTrainData = data;
	}
	
	public String getCvTrain(){
		return this.cvTrainData;
	}
	
	/**
	 * Get document writer defined with the -o and -t options.
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentWriter getOutputWriter() throws Exception{
        String input_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT);
        String output_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
        String output_file = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE);
        AbstractDocumentWriter writer;
        if (output_format == null){
            writer = WriterFactory.get().getStreamWriter(System.out, output_format);        	
        }
        else if (output_format.equals("arff")){
            Template arff_template = LinerOptions.getGlobal().getArffTemplate();
            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
        }
        else if ( output_format.equals("ccl-batch") ){
        	if ( !input_format.equals("ccl-batch") )
        		throw new Exception("Output format `ccl-batch` (-o) is valid only for `ccl-batch` input format (-i).");        	
        	writer = new CclBatchWriter(output_file);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        }
        return writer;
	}

	/**
	 * Get document reader defined with the -i and -f options.
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentReader getInputReader() throws Exception{
        return ReaderFactory.get().getStreamReader(
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));        
	}
	
	@SuppressWarnings("static-access")
	private Options makeOptions(){
    	Options options = new Options();    	

    	options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("load chunker from chunker factory")
				.create(OPTION_CHUNKER));
    	options.addOption(OptionBuilder.withArgName("filename").hasArg()
				.withDescription("loads a list of common words from a file")
				.create("common"));
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.withDescription("database host name (daemon mode)")
				.create(OPTION_DB_HOST));
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.withDescription("database name (daemon mode)")
				.create(OPTION_DB_NAME));
		options.addOption(OptionBuilder.withArgName("password").hasArg()
				.withDescription("database password (daemon mode)")
				.create(OPTION_DB_PASSWORD));
		options.addOption(OptionBuilder.withArgName("number").hasArg()
				.withDescription("database port number (daemon mode)")
				.create(OPTION_DB_PORT));
		options.addOption(OptionBuilder.withArgName("address").hasArg()
				.withDescription("database URI address (daemon mode)")
				.create(OPTION_DB_URI));
		options.addOption(OptionBuilder.withArgName("username").hasArg()
				.withDescription("database user name (daemon mode)")
				.create(OPTION_DB_USER));
    	options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("recognized feature name")
				.create(OPTION_FEATURE));
    	options.addOption(OptionBuilder.withArgName("num").hasArg()
				.withDescription("number of folds")
				.create(OPTION_FOLDS_NUMBER));
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
		options.addOption(OptionBuilder
                 .withArgName("filename").hasArg().withDescription("name of file with list of input files")
                 .create(OPTION_IS));
		options.addOption(OptionBuilder.withArgName("filename").hasArg()
				.withDescription("read input from file")
				.create(OPTION_INPUT_FILE));
		options.addOption(OptionBuilder.withArgName("format").hasArg()
				.withDescription("input format [plain,iob,ccl,ccl-batch]")
				.create(OPTION_INPUT_FORMAT));
		options.addOption(OptionBuilder.withArgName("address").hasArg()
				.withDescription("IP address for daemon")
				.create(OPTION_IP));
		options.addOption(OptionBuilder.withArgName("number").hasArg()
				.withDescription("maximum number of processing threads (daemon mode)")
				.create(OPTION_MAX_THREADS));
    	options.addOption(OptionBuilder.withArgName("number").hasArg()
				.withDescription("port to listen on (daemon mode)")
				.create(OPTION_PORT));
    	options.addOption(OptionBuilder.withArgName("types").hasArg()
				.withDescription("types of annotation to evaluate")
				.create(OPTION_TYPES));
		options.addOption(OptionBuilder.withArgName("filename").hasArg()
				.withDescription("save output to file")
				.create(OPTION_OUTPUT_FILE));
		options.addOption(OptionBuilder.withArgName("chunkers").hasArg()
				.withDescription("specify chunkers to use")
				.create(OPTION_USE));
		options.addOption(OptionBuilder.withArgName("format").hasArg()
				.withDescription("output format [iob,ccl,tuples]")
				.create(OPTION_OUTPUT_FORMAT));
		options.addOption(OptionBuilder.withArgName("description").hasArg()
				.withDescription("define feature template")
				.create(OPTION_TEMPLATE));
    	options.addOption(new Option(OPTION_SILENT, false, "does not print any additional text in interactive mode"));
    	options.addOption(new Option(OPTION_VERBOSE, false, "print brief information about processing"));
    	options.addOption(new Option(OPTION_VERBOSE_DETAILS, false, "print detailed information about processing"));
    	return options;		
	}
	
    /**
     * Prints program usage and description.
     */
    public void printHelp(){
    	System.out.println("--------------------------------------------------*");
    	System.out.println("* A tool for Named Entity Recognition for Polish. *");
    	System.out.println("*       Authors: Michał Marcińczuk (2010–2014)    *");
    	System.out.println("*                Maciej Janicki (2011)            *");
    	System.out.println("*  Contributors: Dominik Piasecki (2013)          *");
    	System.out.println("*                Michał Krautforst (2013-2014)    *");
    	System.out.println("*   Institution: Wrocław University of Technology.*");
    	System.out.println("--------------------------------------------------*");
    	System.out.println();
		new HelpFormatter().printHelp("java -jar liner.jar <mode> [options]", options);
		System.out.println();
    	System.out.println("Modes:");
    	System.out.println("  interactive         - interactive mode");
    	System.out.println("                        Parameters: -ini, -o");
        System.out.println("  batch               - process list of files");
        System.out.println("                        Parameters: -i, -o, -is, -ini");
    	System.out.println("  convert             - convert text from one format to another");
    	System.out.println("                        Parameteres: -i, -o, -f, -t, -ini");
       	System.out.println("  daemon              - Listen and process requests from a given database");
    	System.out.println("                        Parameteres: -p, -db_*, -ini");
    	System.out.println("  eval                - evaluate chunker on given input");
    	System.out.println("                        Parameters: -i, -f, -ini");
    	System.out.println("  evalcv              - perform 10-fold cross validation");
    	System.out.println("                        Parameters: -i, -f, -ini");
    	System.out.println("  pipe                - annotate data");
    	System.out.println("                        Parameters: -i, (-f), -o, (-t), -ini");
    	System.out.println("  train               - train CRFPP chunker");
    	System.out.println("                        Parameters: -ini");
    	System.out.println("  time                - measure processing time");
    	System.out.println("                        Parameters: -i, (-f), -o, (-t), -ini");
    	System.out.println("");
    	System.out.println("");
    	System.out.println("Chunker factory (patterns for `-chunker` parameter):");
    	System.out.println(ChunkerFactory.getDescription());
    }	

	public void printConfigurationDescription(){
		Main.log(this.configurationDescription);
	}
    
}
