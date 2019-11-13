package g419.lib.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;

/**
 * This class contains methods to generate Option objects for common options.
 *
 * @author czuk
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

  public static final String OPTION_CLASSIFIER_MODEL = "c";
  public static final String OPTION_CLASSIFIER_MODEL_LONG = "classifier_model";

  public static final String OPTION_WORDNET = "w";
  public static final String OPTION_WORDNET_LONG = "wordnet";

  public static final String OPTION_MALT = "M";
  public static final String OPTION_MALT_LONG = "malt";
  public static final String OPTION_MALT_DESC = "path to maltparser model";
  public static final String OPTION_MALT_ARG = "path";

  public static final String OPTION_ANNOTATION_PATTERN = "A";
  public static final String OPTION_ANNOTATION_PATTERN_LONG = "annotation-type-pattern";
  public static final String OPTION_ANNOTATION_PATTERN_DESC = "pattern of annotation type";

  public static Option getOutputFileNameOption() {
    return Option.builder(CommonOptions.OPTION_OUTPUT_FILE)
        .longOpt(CommonOptions.OPTION_OUTPUT_FILE_LONG)
        .hasArg().argName("filename").desc("path to an output file").build();
  }

  public static Option getOutputFileFormatOption() {
    return Option.builder(CommonOptions.OPTION_OUTPUT_FORMAT)
        .longOpt(CommonOptions.OPTION_OUTPUT_FORMAT_LONG)
        .hasArg().argName("filename").desc("output format [iob, ccl, arff, tokens, tuples, tei, batch:{format}]").build();
  }

  public static Option getInputFileNameOption() {
    return Option.builder(CommonOptions.OPTION_INPUT_FILE)
        .longOpt(CommonOptions.OPTION_INPUT_FILE_LONG)
        .hasArg().argName("filename").desc("path to a file to read").build();
  }

  public static Option getInputFileNamesOption() {
    return Option.builder(CommonOptions.OPTION_INPUT_FILE)
        .longOpt(CommonOptions.OPTION_INPUT_FILE_LONG)
        .hasArgs().argName("filenames").desc("list of paths, i.e. 'path1 path2 path3 ...'").build();
  }

  public static Option getInputFileFormatOption() {
    return Option.builder(CommonOptions.OPTION_INPUT_FORMAT)
        .longOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG)
        .hasArg().argName("format").desc("input format [iob, ccl, plain, plain:maca, plain:wcrft, tei, batch:{format}]").build();
  }

  public static Option getInputFileFormatsOption() {
    return Option.builder(CommonOptions.OPTION_INPUT_FORMAT)
        .longOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG)
        .hasArgs().argName("format").desc("input format [iob, ccl, plain, plain:maca, plain:wcrft, tei, batch:{format}]").build();
  }

  public static Option getFeaturesOption() {
    return getFeaturesOption(false);
  }

  public static Option getFeaturesOption(final boolean required) {
    Option.Builder op = Option.builder(CommonOptions.OPTION_FEATURES)
        .longOpt(CommonOptions.OPTION_FEATURES_LONG)
        .hasArg().argName("features").desc("a file with a list of features");
    if (required) {
      op = op.required();
    }
    return op.build();
  }

  public static Option getClassifierModelFile() {
    return Option.builder(CommonOptions.OPTION_CLASSIFIER_MODEL)
        .longOpt(CommonOptions.OPTION_CLASSIFIER_MODEL_LONG)
        .hasArg().argName("classifier model").desc("file with classifier model").build();
  }

  public static Option getModelFileOption() {
    return Option.builder(CommonOptions.OPTION_MODEL)
        .longOpt(CommonOptions.OPTION_MODEL_LONG)
        .required()
        .hasArg().argName("model").desc("file with model configuration").build();
  }

  public static Option getWordnetOption(final boolean required) {
    Builder b = Option.builder(OPTION_WORDNET).longOpt(OPTION_WORDNET_LONG)
        .hasArg().argName("path").desc("path to a folder with a wordnet in Princeton format");
    if (required) {
      b = b.required();
    }
    return b.build();
  }

  public static Option getVerboseOption() {
    return Option.builder(CommonOptions.OPTION_VERBOSE)
        .longOpt(CommonOptions.OPTION_VERBOSE_LONG)
        .desc("print help").build();
  }

  public static Option getVerboseDeatilsOption() {
    return Option.builder(CommonOptions.OPTION_VERBOSE_DETAILS)
        .longOpt(CommonOptions.OPTION_VERBOSE_DETAILS_LONG)
        .desc("verbose processed sentences data").build();
  }

  public static Option getInputFileFormatOptionWithAnnotations() {
    return Option.builder(CommonOptions.OPTION_INPUT_FORMAT)
        .longOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG)
        .hasArg().argName("format").desc("input format [iob, ccl, tei, batch:{format}]").build();
  }

  public static Option getMaltparserModelFileOption() {
    return Option.builder(OPTION_MALT).longOpt(OPTION_MALT_LONG).hasArg().argName(OPTION_MALT_ARG)
        .desc(OPTION_MALT_DESC).build();
  }

  public static Option getAnnotationTypePatterns() {
    return Option.builder(OPTION_ANNOTATION_PATTERN)
        .longOpt(OPTION_ANNOTATION_PATTERN_LONG)
        .desc(OPTION_ANNOTATION_PATTERN_DESC)
        .hasArgs()
        .build();
  }

}
