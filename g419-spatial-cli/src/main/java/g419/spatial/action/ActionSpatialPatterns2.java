package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.converter.DocumentToSpatialExpressionConverter;
import g419.spatial.pattern.AnnotationPatternGenerator;
import g419.spatial.structure.SpatialExpression;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ActionSpatialPatterns2 extends Action {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  final private AnnotationPatternGenerator generator = new AnnotationPatternGenerator();
  private String filename = null;
  private String inputFormat = null;

  private final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

  public ActionSpatialPatterns2() {
    super("spatial-patterns2");
    setDescription("new implementation of pattern generator for spatial expressions (includes static and dynamic expressions)");
    options.addOption(getOptionInputFilename());
    options.addOption(CommonOptions.getInputFileFormatOption());
  }

  /**
   * Create Option object for input file name.
   *
   * @return Object for input file name parameter.
   */
  private Option getOptionInputFilename() {
    return Option.builder(CommonOptions.OPTION_INPUT_FILE).hasArg().argName("path").required()
        .desc("path to the input file").longOpt(CommonOptions.OPTION_INPUT_FILE_LONG).build();
  }

  /**
   * Parse action options
   *
   * @param args The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
  }

  @Override
  public void run() throws Exception {
    final List<String> patterns = Lists.newArrayList();
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat)) {
      while (reader.hasNext()) {
        final Document document = reader.nextDocument();
        final List<SpatialExpression> ses = converter.convert(document);
        ses.stream().map(r -> generatePattern(r)).forEach(patterns::add);
      }
    }
    final Map<String, Integer> count = Maps.newHashMap();
    patterns.stream().forEach(p -> count.put(p, count.getOrDefault(p, 0) + 1));
    final List<Map.Entry<String, Integer>> items = Lists.newArrayList(count.entrySet());
    items.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).thenComparing(Map.Entry::getKey));
    items.stream().map(p -> String.format("[%3d] %s", p.getValue(), p.getKey())).forEach(System.out::println);
  }

  /**
   * Tworzy wzorzec kontekstu zawierającego wszystkie wskazane anotacje.
   *
   * @param se
   * @return
   */
  private String generatePattern(final SpatialExpression se) {
    final List<Annotation> annotations = Lists.newArrayList(se.getAnnotations());
    String str = "";
    if (annotations.size() > 0) {
      try {
        str = generator.generate(annotations.get(0).getSentence(), annotations);
      } catch (final Exception ex) {
        logger.warn("Failed to generate pattern for spatial expression: {}", se, ex);
      }
    }
    return str;
  }

}
