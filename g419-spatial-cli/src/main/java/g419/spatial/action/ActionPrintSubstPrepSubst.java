package g419.spatial.action;

import com.google.common.collect.Lists;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.CsvGenericWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.schema.annotation.NkjpSpejd;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.converter.DocumentToSpatialExpressionConverter;
import g419.spatial.filter.RelationFilterSemanticPattern;
import g419.spatial.pattern.SentencePattern;
import g419.spatial.pattern.SentencePatternMatchAnnotationPattern;
import g419.spatial.pattern.SentencePatternMatchSequence;
import g419.spatial.pattern.SentencePatternMatchTokenPos;
import g419.spatial.structure.SpatialExpression;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.cli.CommandLine;

public class ActionPrintSubstPrepSubst extends Action {

  private String inputFilename = null;
  private String inputFormat = null;
  private String outputFilename = null;
  RelationFilterSemanticPattern semanticPatterns = null;

  SentencePattern pattern = null;
  DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

  public ActionPrintSubstPrepSubst() {
    super("print-subst-prep-subst");
    setDescription("prints a list of subst-prep-subst phrases");
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
    pattern = new SentencePattern("NG*_Prep_NG*",
        new SentencePatternMatchSequence()
            .append(new SentencePatternMatchAnnotationPattern(NkjpSpejd.NGAny)
                .withLabel(SpatialExpression.TRAJECTOR))
            .append(new SentencePatternMatchTokenPos("prep")
                .withLabel(SpatialExpression.SPATIAL_INDICATOR))
            .append(new SentencePatternMatchAnnotationPattern(NkjpSpejd.NGAny)
                .withLabel(SpatialExpression.LANDMARK))
    );

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
    final Set<String> spatialExpressionAsString = getSpatialExpressionsAsStrings(document);
    return document.getSentences().stream()
        .map(this::process)
        .flatMap(Collection::stream)
        .map(frame -> convert(frame, spatialExpressionAsString))
        .collect(Collectors.toList());

  }

  private Set<String> getSpatialExpressionsAsStrings(final Document document) {
    return converter.convert(document).stream()
        .filter(se -> se.getTrajector().getSpatialObject() != null)
        .filter(se -> se.getLandmark().getSpatialObject() != null)
        .filter(se -> se.getSpatialIndicator() != null)
        .map(this::spatialToString)
        .collect(Collectors.toSet());
  }

  private String spatialToString(final SpatialExpression se) {
    return String.format("%s_%s_%s",
        se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getBase(),
        se.getSpatialIndicator().getBaseText().toLowerCase(),
        se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase());
  }

  private List<Frame<Annotation>> process(final Sentence sentence) {
    final SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence);
    return pattern.match(index)
        .stream()
        .filter(f -> validSubst(f.getSlot(SpatialExpression.TRAJECTOR)))
        .filter(f -> validSubst(f.getSlot(SpatialExpression.LANDMARK)))
        .collect(Collectors.toList());
  }

  private String frameToString(final Frame<Annotation> frame) {
    return String.format("%s_%s_%s",
        frame.getSlot(SpatialExpression.TRAJECTOR).getHeadToken().getDisambTag().getBase(),
        frame.getSlot(SpatialExpression.SPATIAL_INDICATOR).getHeadToken().getDisambTag().getBase(),
        frame.getSlot(SpatialExpression.LANDMARK).getHeadToken().getDisambTag().getBase());
  }

  private List<String> convert(final Frame<Annotation> frame,
                               final Set<String> spatialExpressionAsString) {
    final List<String> record = Lists.newArrayList();
    record.add(frame.getSlot(SpatialExpression.TRAJECTOR).getSentence().getDocument().getName());
    record.add(frame.getSlot(SpatialExpression.TRAJECTOR).getText());
    record.add(frame.getSlot(SpatialExpression.TRAJECTOR).getHeadToken().getOrth());
    record.add(frame.getSlot(SpatialExpression.TRAJECTOR).getHeadToken().getDisambTag().getBase());
    record.add(frame.getSlot(SpatialExpression.TRAJECTOR).getHeadToken().getDisambTag().getPos());
    record.add(frame.getSlot(SpatialExpression.TRAJECTOR).getHeadToken().getDisambTag().getCtag());
    record.add(frame.getSlot(SpatialExpression.SPATIAL_INDICATOR).getText());
    record.add(frame.getSlot(SpatialExpression.LANDMARK).getText());
    record.add(frame.getSlot(SpatialExpression.LANDMARK).getHeadToken().getOrth());
    record.add(frame.getSlot(SpatialExpression.LANDMARK).getHeadToken().getDisambTag().getBase());
    record.add(frame.getSlot(SpatialExpression.LANDMARK).getHeadToken().getDisambTag().getPos());
    record.add(frame.getSlot(SpatialExpression.LANDMARK).getHeadToken().getDisambTag().getCtag());

    final String key = frameToString(frame);
    record.add(spatialExpressionAsString.contains(key) ? "label-spatial" : "");

    return record;
  }

  private boolean validSubst(final Annotation an) {
    return an.getHeadToken().getDisambTag().getPos().equals("subst");
  }

}