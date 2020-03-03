package g419.spatial.action;

import com.google.common.collect.Lists;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.CsvGenericWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.SentenceAnnotationIndexTypePos;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.converter.DocumentToSpatialExpressionConverter;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialObjectRegion;
import io.vavr.control.Option;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.cli.CommandLine;

public class ActionPrintLabelSpatial extends Action {

  private String inputFilename = null;
  private String inputFormat = null;
  private String outputFilename = null;

  DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

  public ActionPrintLabelSpatial() {
    super("print-label-spatial");
    setDescription("prints a list of tr-si-lm phrases");
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
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
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
    header.add("document");
    header.add("trajector");
    header.add("trajector_head");
    header.add("trajector_lemma");
    header.add("trajector_pos");
    header.add("trajector_ctag");
    header.add("spatial_indicator");
    header.add("landmark");
    header.add("landmark_head");
    header.add("landmark_lemma");
    header.add("landmark_pos");
    header.add("landmark_ctag");
    header.add("label");
    return header;
  }

  private List<List<String>> process(final Document document) {
    return converter.convert(document).stream()
        .filter(se -> validSpatialObject(se.getTrajector()))
        .filter(se -> validSpatialObject(se.getLandmark()))
        .filter(se -> se.getSpatialIndicator() != null)
        .map(this::convert)
        .collect(Collectors.toList());

  }

  private List<String> convert(final SpatialExpression se) {
    final SentenceAnnotationIndexTypePos index
        = new SentenceAnnotationIndexTypePos(se.getSpatialIndicator().getSentence());
    final Option<Annotation> trPhrase =
        index.getLongestOfTypeAtPos("phrase", se.getTrajector().getSpatialObject().getBegin());
    final Option<Annotation> lmPhrase =
        index.getLongestOfTypeAtPos("phrase", se.getLandmark().getSpatialObject().getBegin());
    final List<String> record = Lists.newArrayList();
    record.add(se.getSpatialIndicator().getSentence().getDocument().getName());
    record.add(trPhrase.getOrElse(se.getTrajector().getSpatialObject()).getText());
    record.add(se.getTrajector().getSpatialObject().getHeadToken().getOrth());
    record.add(se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getBase());
    record.add(se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getPos());
    record.add(se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getCtag());
    record.add(se.getSpatialIndicator().getText());
    record.add(lmPhrase.getOrElse(se.getLandmark().getSpatialObject()).getText());
    record.add(se.getLandmark().getSpatialObject().getHeadToken().getOrth());
    record.add(se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase());
    record.add(se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getPos());
    record.add(se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getCtag());
    record.add("label-spatial");
    return record;
  }

  private boolean validSpatialObject(final SpatialObjectRegion sor) {
    return sor.getSpatialObject() != null
        && sor.getSpatialObject().getHeadToken().getDisambTag().getPos().equals("subst");
  }

}