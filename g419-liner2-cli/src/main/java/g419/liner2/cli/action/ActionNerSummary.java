package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.NerSummary;
import org.apache.commons.cli.CommandLine;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionNerSummary extends Action {

  private String inputFile = null;
  private String inputFormat = null;
  private List<Pattern> typePatterns = Lists.newArrayList();
  private final int topNPerGroup = 10;

  private final NerSummary nerSummary = new NerSummary();

  public ActionNerSummary() {
    super("ner-summary");
    setDescription("generates a summary of named entities for given set of documents");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getAnnotationTypePatterns());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    if (line.hasOption(CommonOptions.OPTION_ANNOTATION_PATTERN)) {
      typePatterns = Stream.of(line.getOptionValues(CommonOptions.OPTION_ANNOTATION_PATTERN))
          .map(Pattern::compile)
          .collect(Collectors.toList());
    }
  }

  @Override
  public void run() throws Exception {
    collectAnnotations();
    printGroups();
  }

  private void collectAnnotations() throws Exception {
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat)) {
      reader.forEach(document -> nerSummary.addAll(document.getAnnotations(typePatterns)));
    }
  }

  private void printGroups() {
    getLogger().debug("Groups: " + nerSummary.getGroups().size());
    nerSummary.getGroups().stream()
        .sorted((g1, g2) -> Integer.compare(g2.getScore(), g1.getScore()))
        .filter(g -> g.getScore() > 1.0)
        .collect(Collectors.groupingBy(NerSummary.AnnotationGroup::getType))
        .entrySet().stream()
        .forEach(e -> printTypeGroups(e.getKey(), takeTopN(e.getValue())));
  }

  private List<NerSummary.AnnotationGroup> takeTopN(final List<NerSummary.AnnotationGroup> groups) {
    return groups.stream()
        .sorted((g1, g2) -> Integer.compare(g2.getAnnotations().size(), g1.getAnnotations().size()))
        .limit(topNPerGroup)
        .collect(Collectors.toList());
  }

  private void printTypeGroups(final String type, final List<NerSummary.AnnotationGroup> groups) {
    groups.stream().map(this::groupToString).forEach(System.out::println);
  }

  private String groupToString(final NerSummary.AnnotationGroup group) {
    return String.format("%d\t%s\t%s", group.getScore(), group.getType(), group.getName());
  }

}
