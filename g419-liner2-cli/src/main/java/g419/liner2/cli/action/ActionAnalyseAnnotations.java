package g419.liner2.cli.action;


import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import lombok.Data;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ActionAnalyseAnnotations extends Action {

  private static final String PARAM_TRAIN_FORMAT = "r";
  private static final String PARAM_TRAIN_PATH = "p";
  private static final String PARAM_TRAIN_PATH_DESC = "path to the training dataset";

  ActionAnalyseAnnotationsParams params = null;

  public ActionAnalyseAnnotations() {
    super("analyse-annotations");
    setDescription("analyze annotation results comparing the training dataset");
    options.addOption(getTrainFormatOption());
    options.addOption(getTrainPathOption());
  }

  public static Option getTrainFormatOption() {
    return Option.builder(PARAM_TRAIN_FORMAT)
        .longOpt(CommonOptions.OPTION_INPUT_FORMAT_LONG)
        .hasArg().argName("format").desc(CommonOptions.OPTION_INPUT_TYPES).build();
  }

  public static Option getTrainPathOption() {
    return Option.builder(PARAM_TRAIN_PATH)
        .longOpt(PARAM_TRAIN_PATH_DESC)
        .hasArg().argName("path").build();
  }


  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    final ActionAnalyseAnnotationsParams params = new ActionAnalyseAnnotationsParams();
    params.setTrainPath(line.getOptionValue(PARAM_TRAIN_PATH));
    params.setTrainFormat(line.getOptionValue(PARAM_TRAIN_FORMAT));
    this.params = params;
  }

  /**
   *
   */
  @Override
  public void run() throws Exception {
    final List<Document> trainDataset = loadTrainDataset();

    final List<Annotation> ans = trainDataset.stream()
        .map(Document::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList());

    final Map<Integer, List<Annotation>> annsByLength = ans.
        stream().collect(Collectors.groupingBy(Annotation::getTokenCount));

    annsByLength.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
        .map(e -> String.format("%3d token(s): %4d (%5.2f)",
            e.getKey(), e.getValue().size(), (double) e.getValue().size() * 100.0 / (double) ans.size()))
        .forEach(System.out::println);
  }

  private List<Document> loadTrainDataset() throws Exception {
    return ReaderFactory.loadDocuments(params.getTrainPath(), params.getTrainFormat());
  }

  @Data
  class ActionAnalyseAnnotationsParams {
    String trainFormat;
    String trainPath;
  }

}
