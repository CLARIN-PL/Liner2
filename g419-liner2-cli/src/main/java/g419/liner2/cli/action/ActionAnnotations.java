package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractMatrixWriter;
import g419.corpus.io.writer.ArffGenericWriter;
import g419.corpus.io.writer.CsvGenericWriter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.features.AnnotationFeatureGenerator;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Generates an ARFF file with a list of annotations of defined types
 * with given set of features.
 */
public class ActionAnnotations extends Action {

  public static final String OPTION_ANNOTATION_FEATURE = "a";
  public static final String OPTION_ANNOTATION_FEATURE_LONG = "annotation_features";

  public static final String OPTION_TYPES = "T";
  public static final String OPTION_TYPES_LONG = "types";

  private String output_file = null;
  private String input_file = null;
  private String input_format = null;
  private String features_file = null;
  private List<Pattern> types = new ArrayList<Pattern>();
  Map<String, String> features = new LinkedHashMap<String, String>();

  public ActionAnnotations() {
    super("annotations");
    this.setDescription("generates an arff file with a list of annotations and their features");
    this.options.addOption(Option.builder(ActionAnnotations.OPTION_ANNOTATION_FEATURE)
        .argName(ActionAnnotations.OPTION_ANNOTATION_FEATURE_LONG)
        .hasArg().desc("a file with a list of annotation features")
        .longOpt(ActionAnnotations.OPTION_ANNOTATION_FEATURE_LONG)
        .required()
        .build());

    this.options.addOption(Option.builder(ActionAnnotations.OPTION_TYPES)
        .argName(ActionAnnotations.OPTION_TYPES_LONG)
        .hasArg()
        .desc("a file with a list of annotation name patterns")
        .longOpt(ActionAnnotations.OPTION_TYPES_LONG)
        .build());

    this.options.addOption(CommonOptions.getOutputFileNameOption());
    this.options.addOption(CommonOptions.getInputFileFormatOption());
    this.options.addOption(CommonOptions.getInputFileNameOption());
    this.options.addOption(CommonOptions.getFeaturesOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    this.features_file = line.getOptionValue(ActionAnnotations.OPTION_ANNOTATION_FEATURE);
    String typesFile = line.getOptionValue(OPTION_TYPES);
    if (typesFile != null) {
      this.types = LinerOptions.getGlobal().parseTypes(typesFile);
    }
    /* Parse token features */
    String featuresFile = line.getOptionValue(CommonOptions.OPTION_FEATURES);
    if (featuresFile != null) {

      this.features = LinerOptions.getGlobal().parseFeatures(featuresFile);
    }
  }

  @Override
  public void run() throws Exception {
    List<String> annFeatures = this.parseAnnotationFeatures(this.features_file);
    AnnotationFeatureGenerator annGen = new AnnotationFeatureGenerator(annFeatures);

    AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
        this.input_file, this.input_format);

    AbstractMatrixWriter writer1 = new ArffGenericWriter(new FileOutputStream(this.output_file + ".arff"));
    AbstractMatrixWriter writer2 = new CsvGenericWriter(new FileOutputStream(this.output_file + ".csv"));
    writer1.writeHeader("annotations", annFeatures);
    writer2.writeHeader("annotations", annFeatures);

    TokenFeatureGenerator gen = null;
    if (!this.features.isEmpty()) {
      gen = new TokenFeatureGenerator(this.features);
    }

    Document ps = reader.nextDocument();
    while (ps != null) {
      if (gen != null) {
        gen.generateFeatures(ps);
      }
      for (AnnotationSet annotations : ps.getChunkings().values()) {
        for (Annotation ann : annotations.chunkSet()) {
          if (this.types.isEmpty() || this.isTypeMatched(ann.getType(), this.types)) {
            List<String> values = annGen.generateAtomicFeatures(ann);
            values.add(ann.getType());
            writer1.writeRow(values);
            writer2.writeRow(values);
          }
        }
      }

      ps = reader.nextDocument();
    }
    writer1.close();
    writer2.close();
    reader.close();
  }

  /**
   * @param type
   * @param patterns
   * @return
   */
  private boolean isTypeMatched(String type, List<Pattern> patterns) {
    for (Pattern patt : this.types) {
      if (patt.matcher(type).find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param path
   * @return
   * @throws IOException
   */
  private List<String> parseAnnotationFeatures(String path) throws IOException {
    List<String> annotationFeatures = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new FileReader(path));
    String line = br.readLine();
    while (line != null) {
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("#")) {
        annotationFeatures.add(line);
      }
      line = br.readLine();
    }
    br.close();
    return annotationFeatures;
  }

}
