package g419.lib.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * This class contains methods to generate Option objects for common options.
 * @author czuk
 *
 */
public class CommonOptions {

    public static final String OPTION_VERBOSE = "v";
    public static final String OPTION_VERBOSE_LONG = "verbose";

    public static final String OPTION_VERBOSE_DETAILS = "d";
    public static final String OPTION_VERBOSE_DETAILS_LONG = "details";

    public static final String OPTION_OUTPUT_FORMAT = "o";
    public static final String OPTION_OUTPUT_FORMAT_LONG = "output_format";
	
	public static final String OPTION_OUTPUT_FILE = "t";
	public static final String OPTION_OUTPUT_FILE_LONG = "output_file";

	public static final String OPTION_INPUT_FORMAT = "i";
	public static final String OPTION_INPUT_FORMAT_LONG = "input_format";

	public static final String OPTION_INPUT_FILE = "f";
	public static final String OPTION_INPUT_FILE_LONG = "input_file";

    public static final String OPTION_FEATURES = "F";
    public static final String OPTION_FEATURES_LONG = "features";

    public static final String OPTION_MODEL = "m";
    public static final String OPTION_MODEL_LONG = "model";


	public static Option getOutputFileNameOption(){
		OptionBuilder.withArgName("filename");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("path to an output file");
		OptionBuilder.withLongOpt(CommonOptions.OPTION_OUTPUT_FILE_LONG);
		return OptionBuilder.create(CommonOptions.OPTION_OUTPUT_FILE);		
	}

    public static Option getOutputFileFormatOption(){       
        OptionBuilder.withArgName("filename");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("output format [iob, ccl, arff, tokens, tuples, tei, batch:{format}]");
        OptionBuilder.withLongOpt(CommonOptions.OPTION_OUTPUT_FORMAT_LONG);
        return OptionBuilder.create(CommonOptions.OPTION_OUTPUT_FORMAT);
    }

	public static Option getInputFileNameOption(){
		OptionBuilder.withArgName("filename");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("read input from file");
		OptionBuilder.withLongOpt(CommonOptions.OPTION_INPUT_FILE_LONG);
		OptionBuilder.isRequired();
		return OptionBuilder.create(OPTION_INPUT_FILE);
	}
	
	public static Option getInputFileFormatOption(){
		OptionBuilder.withArgName("format");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("input format [iob, ccl, plain, plain:maca, plain:wcrft, tei, batch:{format}]");
		OptionBuilder.withLongOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG);
		return OptionBuilder.create(OPTION_INPUT_FORMAT);
	}

    public static Option getFeaturesOption(){
        OptionBuilder.withArgName("features");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("a file with a list of features");
        OptionBuilder.withLongOpt(CommonOptions.OPTION_FEATURES_LONG);
        return OptionBuilder.create(CommonOptions.OPTION_FEATURES);
    }

    public static Option getModelFileOption(){
    	OptionBuilder.withArgName("model");
    	OptionBuilder.hasArg();
        OptionBuilder.withDescription("file with model configuration");
        OptionBuilder.withLongOpt(CommonOptions.OPTION_MODEL_LONG);
        OptionBuilder.isRequired();
        return OptionBuilder.create(CommonOptions.OPTION_MODEL);
    }

    public static Option getVerboseOption(){
        OptionBuilder.withLongOpt(OPTION_VERBOSE_LONG);
        OptionBuilder.withDescription("print help");
        return OptionBuilder.create(OPTION_VERBOSE);
    }

    public static Option getVerboseDeatilsOption(){
    	OptionBuilder.withDescription("verbose processed sentences data");
        OptionBuilder.withLongOpt(OPTION_VERBOSE_DETAILS_LONG);
        return OptionBuilder.create(OPTION_VERBOSE_DETAILS);
    }

	public static Option getInputFileFormatOptionWithAnnotations(){
		OptionBuilder.withArgName("format");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("input format [iob, ccl, tei, batch:{format}]");
		OptionBuilder.withLongOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG);
		return OptionBuilder.create(OPTION_INPUT_FORMAT);
	}


}
