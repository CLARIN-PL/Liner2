package g419.liner2.cli;

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

    public static final String OPTION_OUTPUT_FORMAT = "o";
    public static final String OPTION_OUTPUT_FORMAT_LONG = "output_format";
	
	public static final String OPTION_OUTPUT_FILE = "t";
	public static final String OPTION_OUTPUT_FILE_LONG = "output_file";

	public static final String OPTION_INPUT_FORMAT = "i";
	public static final String OPTION_INPUT_FORMAT_LONG = "input_format";

	public static final String OPTION_INPUT_FILE = "f";
	public static final String OPTION_INPUT_FILE_LONG = "input_file";

	public static final String OPTION_TYPES = "T";
	public static final String OPTION_TYPES_LONG = "types";

    public static final String OPTION_FEATURES = "F";
    public static final String OPTION_FEATURES_LONG = "features";

    public static final String OPTION_MODEL = "m";
    public static final String OPTION_MODEL_LONG = "model";


	@SuppressWarnings("static-access")
	public static Option getOutputFileNameOption(){
		return OptionBuilder
				.withArgName("filename").hasArg()
				.withDescription("path to an output file")
				.withLongOpt(CommonOptions.OPTION_OUTPUT_FILE_LONG)
				.create(CommonOptions.OPTION_OUTPUT_FILE);		
	}

    @SuppressWarnings("static-access")
    public static Option getOutputFileFormatOption(){
        return OptionBuilder
                .withArgName("filename").hasArg()
                .withDescription("output format")
                .withLongOpt(CommonOptions.OPTION_OUTPUT_FORMAT_LONG)
                .create(CommonOptions.OPTION_OUTPUT_FORMAT);
    }

	@SuppressWarnings("static-access")
	public static Option getInputFileNameOption(){
		return OptionBuilder
				.withArgName("filename").hasArg()
				.withDescription("read input from file")
				.withLongOpt(CommonOptions.OPTION_INPUT_FILE_LONG)
				.isRequired()
				.create(OPTION_INPUT_FILE);
	}
	
	@SuppressWarnings("static-access")
	public static Option getInputFileFormatOption(){
		return OptionBuilder
				.withArgName("format").hasArg()
				.withDescription("input format [iob, ccl, plain, plain:maca, plain:wcrft, tei, batch:{format}]")
				.withLongOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG)
				.create(OPTION_INPUT_FORMAT);
	}

    @SuppressWarnings("static-access")
    public static Option getTypesOption(){
        return OptionBuilder
                .withArgName("types").hasArg()
                .withDescription("a file with a list of annotation name patterns")
                .withLongOpt(CommonOptions.OPTION_TYPES_LONG)
                .create(CommonOptions.OPTION_TYPES);
    }

    @SuppressWarnings("static-access")
    public static Option getFeaturesOption(){
        return OptionBuilder
                .withArgName("features").hasArg()
                .withDescription("a file with a list of features")
                .withLongOpt(CommonOptions.OPTION_FEATURES_LONG)
                .create(CommonOptions.OPTION_FEATURES);
    }

    @SuppressWarnings("static-access")
    public static Option getModelFileOption(){
        return OptionBuilder
                .withArgName("model").hasArg()
                .withDescription("file with model configuration")
                .withLongOpt(CommonOptions.OPTION_MODEL_LONG)
                .isRequired()
                .create(CommonOptions.OPTION_MODEL);
    }

    @SuppressWarnings("static-access")
    public static Option getVerboseOption(){
        return OptionBuilder
                .withLongOpt(OPTION_VERBOSE_LONG).withDescription("print help")
                .create(OPTION_VERBOSE);
    }


}
