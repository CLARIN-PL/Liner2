package g419.liner2.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * This class contains methods to generate Option objects for common options.
 * @author czuk
 *
 */
public class CommonOptions {
	
	public static final String OPTION_OUTPUT_FILE = "t";
	public static final String OPTION_OUTPUT_FILE_LONG = "output_file";

	public static final String OPTION_INPUT_FORMAT = "i";
	public static final String OPTION_INPUT_FORMAT_LONG = "input_format";

	public static final String OPTION_INPUT_FILE = "f";
	public static final String OPTION_INPUT_FILE_LONG = "input_file";

	public static final String OPTION_TYPES = "T";
	public static final String OPTION_TYPES_LONG = "types";

	@SuppressWarnings("static-access")
	public static Option getOutputFileOption(){
		return OptionBuilder
				.withArgName("filename").hasArg()
				.withDescription("path to an output file")
				.withLongOpt(CommonOptions.OPTION_OUTPUT_FILE_LONG)
				.create(CommonOptions.OPTION_OUTPUT_FILE);		
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
	public static Option getInputFileFormatOptionWithAnnotations(){
		return OptionBuilder
				.withArgName("format").hasArg()
				.withDescription("input format [iob, ccl, tei, batch:{format}]")
				.withLongOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG)
				.create(OPTION_INPUT_FORMAT);
	}

}
