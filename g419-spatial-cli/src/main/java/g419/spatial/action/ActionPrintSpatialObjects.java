package g419.spatial.action;

import com.google.common.collect.Lists;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.CsvGenericWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.filter.RelationFilterSemanticPattern;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.cli.CommandLine;

public class ActionPrintSpatialObjects extends Action {

  private String inputFilename = null;
  private String inputFormat = null;
  private String outputFilename = null;
  RelationFilterSemanticPattern semanticPatterns = null;

  public ActionPrintSpatialObjects() {
    super("print-spatial-objects");
    setDescription("prints list of gold-standard spatial objects with their attributes");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
  }

  @Override
  public void run() throws Exception {
    semanticPatterns = new RelationFilterSemanticPattern();
    final AbstractDocumentReader reader =
        ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
    final List<List<String>> records = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(reader, Spliterator.ORDERED), false)
        .map(this::process)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    final CsvGenericWriter writer = new CsvGenericWriter(
        WriterFactory.get().getOutputStreamFileOrOut(outputFilename));
    writer.writeRow(getHeader());
    records.forEach(row -> {
      try {
        writer.writeRow(row);
      } catch (final Exception ex) {
        getLogger().error("Failed to write record", ex);
      }
    });
    writer.close();
  }

  private List<String> getHeader() {
    final List<String> header = Lists.newArrayList();
    header.add("Document");
    header.add("Annotation id");
    header.add("Spatial object");
    header.add("POS");
    header.add("Ctag");
    header.add("Named entity type");
    header.add("Named entity text");
    header.add("Orth and NE concepts");
    header.add("Mention");
    header.add("Mention text");
    header.add("Cluster size");
    header.add("Cluster concepts");
    header.add("Cluster bases");
    return header;
  }

  private List<List<String>> process(final Document document) {
    final DocumentAnnotationIndexTypePos index = new DocumentAnnotationIndexTypePos(document);
    return document.getAnnotations().stream()
        .filter(an -> KpwrSpatial.SPATIAL_ANNOTATION_SPATIAL_OBJECT.equals(an.getType()))
        .map(an -> convert(an, document, index))
        .collect(Collectors.toList());
  }

  private List<String> convert(final Annotation an,
                               final Document document,
                               final DocumentAnnotationIndexTypePos index) {
    final List<String> values = Lists.newArrayList();
    values.add(an.getSentence().getDocument().getName());
    values.add(an.getId());
    values.add(an.getText());
    values.add(an.getHeadToken().getDisambTag().getPos());
    values.add(an.getHeadToken().getDisambTag().getCtag());

    final Optional<Annotation> nam =
        index.getAnnotationsOfTypeAtHeadPos(an, Pattern.compile("nam_.*")).stream()
            .sorted(Comparator.comparing(Annotation::length).reversed())
            .findFirst();

    values.add(nam.isPresent() ? nam.get().getType() : "");
    values.add(nam.isPresent() ? nam.get().getText() : "");

    values.add(String.join(", ",
        semanticPatterns.getAnnotationConcepts(nam.isPresent() ? nam.get() : an)));

    final Optional<Annotation> men =
        index.getAnnotationsOfTypeAtHeadPos(an, "mention").stream()
            .sorted(Comparator.comparing(Annotation::length).reversed())
            .findFirst();
    values.add(men.isPresent() ? men.get().getType() : "");
    values.add(men.isPresent() ? men.get().getText() : "");

    final Optional<AnnotationCluster> cluster = men.isPresent() ?
        document.getAnnotationClusters().getExistingClusterWithAnnotation(men.get()) : Optional.empty();
    values.add(cluster.isPresent()
        ? "" + cluster.get().getAnnotations().size() : "");
    values.add(cluster.isPresent()
        ? "" + String.join(", ", getClusterConcepts(cluster.get())) : "");
    values.add(cluster.isPresent()
        ? "" + String.join(", ", getClusterHeads(cluster.get())) : "");
    return values;
  }

  private List<String> getClusterConcepts(final AnnotationCluster cluster) {
    return cluster.getAnnotations().stream()
        .map(semanticPatterns::getAnnotationConcepts)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet())
        .stream().sorted().collect(Collectors.toList());
  }

  private List<String> getClusterHeads(final AnnotationCluster cluster) {
    return cluster.getAnnotations().stream()
        .map(Annotation::getHeadToken)
        .map(Token::getDisambTag)
        .map(Tag::getBase)
        .collect(Collectors.toSet())
        .stream().sorted().collect(Collectors.toList());
  }
}