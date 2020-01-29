package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.SentenceAnnotationIndexTypePos;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.FscoreEvaluator;
import g419.spatial.converter.DocumentToSpatialExpressionConverter;
import g419.spatial.filter.RelationFilterSemanticPattern;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialObjectRegion;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.tools.SpatialExpressionKeyGeneratorSimple;
import g419.spatial.tools.SpatialResources;
import g419.toolbox.sumo.Sumo;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.cli.CommandLine;

public class ActionEvalSemanticPatterns extends Action {

  final private Pattern patternAnnotationNam = Pattern.compile("^nam(_(fac|liv|loc|pro|oth).*|$)");

  final private List<Pattern> annotationsPrep = Lists.newLinkedList();
  final private List<Pattern> annotationsNg = Lists.newLinkedList();
  final private Set<String> objectPos = Sets.newHashSet();
  private String filename = null;
  private String inputFormat = null;
  private String wordnetPath = null;

  private Sumo sumo;
  private RelationFilterSemanticPattern semanticPatterns = null;
  private SpatialExpressionKeyGeneratorSimple keyGenerator;
  private final Map<String, FscoreEvaluator> evalByTypeTotal = Maps.newHashMap();
  private final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();
  private final Set<String> regions = SpatialResources.getRegions();

  private final Set<String> elementsToIgnoreByPos = Sets.newHashSet();
  private final Set<String> elementsToIgnoreByBase =
      Sets.newHashSet("ten", "który", "drugi", "jeden");

  private static final String LABEL_SPATIAL = "label-spatial";
  private static final String LABEL_OTHER = "label-other";

  public ActionEvalSemanticPatterns() {
    super("eval-semantic-patterns");
    setDescription("evaluate semantic patterns on gold data");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getWordnetOption(true));
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
  }

  @Override
  public void run() throws Exception {
    loadResources();
    processDocuments();
  }

  private void loadResources() throws IOException {
    sumo = new Sumo(false);
    keyGenerator = new SpatialExpressionKeyGeneratorSimple();
    semanticPatterns = new RelationFilterSemanticPattern();

    annotationsPrep.add(Pattern.compile("^PrepNG.*"));
    annotationsNg.add(Pattern.compile("^NG.*"));
    objectPos.addAll(Sets.newHashSet("subst", "ign", "brev"));

    elementsToIgnoreByPos.addAll(ClassFeature.BROAD_CLASSES.get("verb"));
    elementsToIgnoreByPos.addAll(ClassFeature.BROAD_CLASSES.get("pron"));
  }

  private void processDocuments() throws Exception {
    final List<SpatialExpression> ses = StreamSupport.stream(Spliterators
        .spliteratorUnknownSize(ReaderFactory.get().getStreamReader(filename, inputFormat).iterator(),
            Spliterator.ORDERED), false)
        .map(converter::convert)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    final List<SpatialExpressionEval> evals = ses.stream()
        .filter(se -> se.getTrajector().getSpatialObject() != null)
        .filter(se -> se.getLandmark().getSpatialObject() != null)
        .filter(se -> se.getSpatialIndicator() != null)
        .peek(this::replaceSpatialObjectsWithNames)
        .map(this::evaluateExpression)
        .collect(Collectors.toList());

    final StringJoiner sj = new StringJoiner("\t");
    sj.add("Matched");
    sj.add("Document");
    sj.add("TR id");
    sj.add("TR text");
    sj.add("TR base");
    sj.add("TR ctag");
    sj.add("TR type");
    sj.add("TR WSD");
    sj.add("TR concepts");
    sj.add("SI");
    sj.add("LM id");
    sj.add("LM text");
    sj.add("LM base");
    sj.add("LM ctag");
    sj.add("LM type");
    sj.add("LM WSD");
    sj.add("LM concepts");
    sj.add("Semantic schemas");
    System.out.println(sj.toString());
    evals.stream().map(this::evalToString).forEach(System.out::println);

    final int positive = (int) evals.stream().filter(eval -> eval.getSchemas().size() > 0).count();
    final int negative = evals.size() - positive;
    System.out.println(String.format(" %4d spatial expression(s)", ses.size()));
    System.out.println(
        String.format(" %4d spatial expression(s) with TR, LM and SI", evals.size()));
    System.out.println(
        String.format(" %4d spatial expression(s) matched to a pattern", positive));
    System.out.println(
        String.format(" %4d spatial expression(s) not matched to any pattern", negative));
  }

  private SpatialExpressionEval evaluateExpression(final SpatialExpression spatialExpression) {
    return new SpatialExpressionEval(spatialExpression, semanticPatterns.match(spatialExpression));
  }

  private String evalToString(final SpatialExpressionEval eval) {
    final SpatialExpression se = eval.getSpatialExpression();
    final StringJoiner sj = new StringJoiner("\t");
    sj.add("" + (eval.getSchemas().size() > 0));
    sj.add(se.getSentence().get().getDocument().getName());
    sj.add(se.getTrajector().getSpatialObject().getId());
    sj.add(se.getTrajector().getSpatialObject().getText());
    sj.add(se.getTrajector().getSpatialObject().getBaseText());
    sj.add(se.getTrajector().getSpatialObject().getCtags());
    sj.add(se.getTrajector().getSpatialObject().getType());
    sj.add(semanticPatterns.getHeadSynsetStr(se.getTrajector().getSpatialObject()));
    sj.add(String.join(", ",
        semanticPatterns.getAnnotationConcepts(se.getTrajector().getSpatialObject())));
    sj.add(se.getSpatialIndicator().getLemma());
    sj.add(se.getLandmark().getSpatialObject().getId());
    sj.add(se.getLandmark().getSpatialObject().getText());
    sj.add(se.getLandmark().getSpatialObject().getBaseText());
    sj.add(se.getLandmark().getSpatialObject().getCtags());
    sj.add(se.getLandmark().getSpatialObject().getType());
    sj.add(semanticPatterns.getHeadSynsetStr(se.getLandmark().getSpatialObject()));
    sj.add(String.join(", ",
        semanticPatterns.getAnnotationConcepts(se.getLandmark().getSpatialObject())));
    sj.add(eval.getSchemas().stream().map(SpatialRelationSchema::toString)
        .collect(Collectors.joining(", ")));
    return sj.toString();
  }

  private void replaceSpatialObjectsWithNames(final SpatialExpression se) {
    final SentenceAnnotationIndexTypePos anIndex = new SentenceAnnotationIndexTypePos(
        se.getSentence().get());
    replaceSpatialObjectWithNamedEntity(se.getLandmark(), anIndex);
    replaceSpatialObjectWithNamedEntity(se.getTrajector(), anIndex);
  }

  private void replaceSpatialObjectWithNamedEntity(final SpatialObjectRegion spatialObject,
                                                   final SentenceAnnotationIndexTypePos anIndex) {
    anIndex.getLongestOfTypeAtPos(patternAnnotationNam, spatialObject.getSpatialObject().getBegin())
        .filter(an -> an != spatialObject.getSpatialObject())
        .peek(an -> getLogger().debug("Replace {} ({}) with nam ({})", spatialObject.getSpatialObject().getType(), spatialObject, an))
        .peek(an -> an.setHead(spatialObject.getSpatialObject().getHead()))
        .forEach(spatialObject::setSpatialObject);
  }

  @Data
  @AllArgsConstructor
  private class SpatialExpressionEval {
    SpatialExpression spatialExpression;
    List<SpatialRelationSchema> schemas;

    public SpatialExpressionEval(final SpatialExpression spatialExpression) {
      this.spatialExpression = spatialExpression;
    }
  }

}
