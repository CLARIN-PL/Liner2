package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import io.vavr.control.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionPoleval2019 extends Action {

  private static final String OPTION_NO_LEMMA = "n";
  private static final String OPTION_NO_LEMMA_LONG = "no-lemma";
  private static final String OPTION_NO_LEMMA_DESC = "replace annotation lemmas with 'xx'";
  private static final String OPTION_NO_LEMMA_DEFAULT = "xx";

  private static final String OPTION_EXT_FEATURES = "e";
  private static final String OPTION_EXT_FEATURES_LONG = "extended-features";
  private static final String OPTION_EXT_FEATURES_DESC = "print an extended set of features for phrases";

  private String inputFile = null;
  private String inputFormat = null;
  private String outputFolder = null;
  private boolean extendedFeatures = false;
  private Optional<String> noLemma = Optional.empty();
  private final AtomicInteger globalId = new AtomicInteger();

  public ActionPoleval2019() {
    super("poleval2019-data");
    setDescription("generates data for PolEval 2019 Task 2 on lemmatization");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(getNoLemmaOption());
    options.addOption(getExtendedFeaturesOption());
  }

  private org.apache.commons.cli.Option getNoLemmaOption() {
    return org.apache.commons.cli.Option.builder(OPTION_NO_LEMMA)
        .longOpt(OPTION_NO_LEMMA_LONG)
        .optionalArg(true)
        .numberOfArgs(1)
        .desc(OPTION_NO_LEMMA_DESC)
        .build();
  }

  private org.apache.commons.cli.Option getExtendedFeaturesOption() {
    return org.apache.commons.cli.Option.builder(OPTION_EXT_FEATURES)
        .longOpt(OPTION_EXT_FEATURES_LONG)
        .desc(OPTION_EXT_FEATURES_DESC)
        .build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    outputFolder = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    extendedFeatures = line.hasOption(OPTION_EXT_FEATURES);
    noLemma = getNoLemmaReplacement(line);
  }

  private Optional<String> getNoLemmaReplacement(final CommandLine line) {
    if (line.hasOption(OPTION_NO_LEMMA)) {
      final String[] args = line.getOptionValues(OPTION_NO_LEMMA);
      return Optional.of(args != null && args.length > 0 ? args[0] : OPTION_NO_LEMMA_DEFAULT);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void run() throws Exception {
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
         final CSVPrinter tsv = new CSVPrinter(
             Files.newBufferedWriter(Paths.get(outputFolder, "index.tsv")), CSVFormat.TDF)) {
      reader.forEachRemaining(d -> processDocument(d, tsv, outputFolder));
    }
  }

  private String getShortName(final String name) {
    final String[] parts = name.split("/");
    return parts[parts.length - 1];
  }

  private void processDocument(final Document document, final CSVPrinter csv, final String outputFolder) {
    document.setName(getShortName(document.getName()));
    final List<Annotation> selectedAnnotations = selectAnnotations(document);
    selectedAnnotations.stream()
        .forEach(an -> writeAnnotation(csv, document, an));

    Stream.of(document)
        .peek(d -> retainAnnotations(d, selectedAnnotations))
        .forEach(d -> writeDocument(d, outputFolder));
  }

  private void retainAnnotations(final Document document, final List<Annotation> annotations) {
    document.getSentences().stream().forEach(s -> s.getChunks().retainAll(annotations));
  }

  private void writeDocument(final Document document, final String outputFolder) {
    try {
      WriterFactory.get()
          .getStreamWriter(Paths.get(outputFolder, document.getName() + ".xml").toString(), "inline-annotations")
          .writeDocument(document);
    } catch (final Exception ex) {
      getLogger().error("Exception occured while processing file {}", document.getName(), ex);
    }
  }

  private List<Annotation> selectAnnotations(final Document document) {
    return document.getSentences().stream()
        .map(Sentence::getChunks)
        .flatMap(Collection::stream)
        .filter(an -> an.getLemma() != null)
        .filter(an -> an.getLemma().length() > 0)
        .filter(this::validAnnotationType)
        .filter(this::isNotNumber)
        .peek(Annotation::assignHead)
        .map(this::annotationToKeyAnn)
        .collect(Collectors.groupingBy(Pair::getKey))
        .values()
        .stream()
        .map(List::iterator)
        .map(Iterator::next)
        .map(Pair::getValue)
        .sorted(Comparator.comparing(Annotation::getBegin)
            .thenComparing(Annotation::getTokenCount, Comparator.reverseOrder()))
        .collect(Collectors.toList());
  }

  private void writeAnnotation(final CSVPrinter csv, final Document document, final Annotation annotation) {
    try {
      final List<String> columns = Lists.newArrayList();
      columns.add(annotation.getId());
      columns.add(document.getName());
      columns.add(annotation.getText().trim());
      if (extendedFeatures) {
        columns.add(annotation.getTokenTokens().stream().map(Token::getOrth).collect(Collectors.joining(" ")));
        columns.add(annotation.getTokenTokens().stream().map(Token::getDisambTag)
            .map(Tag::getBase).collect(Collectors.joining(" ")));
        columns.add(annotation.getTokenTokens().stream().map(Token::getDisambTag)
            .map(Tag::getCtag).collect(Collectors.joining(" ")));
        columns.add(annotation.getTokenTokens().stream().map(Token::getNoSpaceAfter).
            map(space -> space ? "False" : "True").collect(Collectors.joining(" ")));
        columns.add(isAnnotationProperName(annotation) ? annotation.getType() : "null");
      }
      columns.add(noLemma.orElse(annotation.getLemma().trim()));
      csv.printRecord(columns);
    } catch (final IOException ex) {
      getLogger().error("Exception thrown while writing to CSV", ex);
    }
  }

  private Pair<String, Annotation> annotationToKeyAnn(final Annotation an) {
    return new ImmutablePair<>(String.format("%d:%d", an.getBegin(), an.getEnd()), an);
  }

  private boolean validAnnotationType(final Annotation an) {
    return isAnnotationMultiwordKeyword(an) || isAnnotationProperName(an);
  }

  private boolean isAnnotationMultiwordKeyword(final Annotation an) {
    return "keyword".equals(an.getType()) && an.getTokens().size() > 1;
  }

  private boolean isAnnotationProperName(final Annotation an) {
    return (Option.of(an.getType()).getOrElse("").startsWith("nam_"));
  }

  private boolean isNotNumber(final Annotation an) {
    return !StringUtils.isNumeric(an.getText());
  }
}
