package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.FscoreEvaluator;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.converter.DocumentToSpatialExpressionConverter;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.*;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionEval2 extends Action {

  final private List<Pattern> annotationsPrep = Lists.newLinkedList();
  final private List<Pattern> annotationsNg = Lists.newLinkedList();
  final private Set<String> objectPos;
  private String filename = null;
  private String inputFormat = null;
  private Optional<String> maltparserModel = null;
  private String wordnetPath = null;
  private String model = null;

  private SpatialExpressionKeyGeneratorSimple keyGenerator;
  private DecisionCollector<SpatialExpression> evalTotalSemanticFilters;
  private DecisionCollector<SpatialExpression> evalTotalCandidates;
  private final Map<String, FscoreEvaluator> evalByTypeTotal = Maps.newHashMap();
  private ISpatialRelationRecognizer recognizer;
  private Sumo sumo;
  private final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();
  private final Set<String> regions = SpatialResources.getRegions();

  private final Set<String> elementsToIgnoreByPos = Sets.newHashSet();
  private final Set<String> elementsToIgnoreByBase = Sets.newHashSet("ten", "który", "drugi", "jeden");

  /**
   *
   */
  public ActionEval2() {
    super("eval2");
    setDescription("evaluate recognition of spatial expressions (new approach including dynamic)");
    options.addOption(getOptionModel());
    options.addOption(getOptionInputFilename());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption());
    options.addOption(CommonOptions.getWordnetOption(true));

    annotationsPrep.add(Pattern.compile("^PrepNG.*"));
    annotationsNg.add(Pattern.compile("^NG.*"));
    objectPos = Sets.newHashSet("subst", "ign", "brev");

    elementsToIgnoreByPos.addAll(ClassFeature.BROAD_CLASSES.get("verb"));
    elementsToIgnoreByPos.addAll(ClassFeature.BROAD_CLASSES.get("pron"));
  }

  private Option getOptionModel() {
    return Option.builder(CommonOptions.OPTION_MODEL).longOpt(CommonOptions.OPTION_MODEL_LONG)
        .hasArg().argName("name").desc("v1|v2").required().build();
  }

  private Option getOptionInputFilename() {
    return Option.builder(CommonOptions.OPTION_INPUT_FILE).hasArg().argName("path").required()
        .desc("path to the input file").longOpt(CommonOptions.OPTION_INPUT_FILE_LONG).build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    maltparserModel = line.hasOption(CommonOptions.OPTION_MALT) ?
        Optional.of(line.getOptionValue(CommonOptions.OPTION_MALT)) : Optional.empty();
    wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
    model = line.getOptionValue(CommonOptions.OPTION_MODEL);
  }

  @Override
  public void run() throws Exception {
    keyGenerator = new SpatialExpressionKeyGeneratorSimple();
    evalTotalSemanticFilters = new DecisionCollector<>(keyGenerator);
    evalTotalCandidates = new DecisionCollector<>(keyGenerator);
    initializeRecognizer();
    sumo = recognizer.getSemanticFilter().getSumo();
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat)) {
      reader.forEach(this::evaluateDocument);
    }
    printEvaluationResult();
  }

  private void printEvaluationResult() {
    printHeader1("With semantic constraints on trajector and landmark");
    evalTotalSemanticFilters.getConfusionMatrix().printTotal();

    printHr1().printHeader2(String.format("%-30s %6s %6s %6s", "Pattern", "P", "TP", "FP"));
    evalByTypeTotal.entrySet().stream()
        .map(p -> formatEvalLine(p.getKey(), p.getValue()))
        .sorted()
        .forEach(System.out::println);

    printHeader1("Without semantic constraints");
    evalTotalCandidates.getConfusionMatrix().printTotal();
    printHr2();
  }

  private void initializeRecognizer() throws IOException, ParameterException {
    final Wordnet3 wordnet = new Wordnet3(wordnetPath);
    switch (model) {
      case "v1":
        recognizer = new SpatialRelationRecognizer(wordnet);
        break;
      case "v2":
        recognizer = new SpatialRelationRecognizer2(wordnet);
        break;
      default:
        throw new ParameterException(String.format("Unrecognized value of '%s', expected: v1|v2", model));
    }
    maltparserModel.ifPresent(m -> recognizer.withMaltParser(new MaltParser(m)));
  }

  private void evaluateDocument(final Document document) {
    Stream.of(document)
        .peek(doc -> printHeader1("Document: " + doc.getName()))
        .map(converter::convert)
        .flatMap(Collection::stream)
        .filter(this::notIgnoredElement)
        .filter(e -> e.getWidth() < 6)
        .peek(evalTotalSemanticFilters::addGold)
        .forEach(evalTotalCandidates::addGold);

    document.getSentences().stream()
        .peek(sentence -> printHeader2("Sentence: " + sentence))
        .peek(this::evaluateSentence)
        .forEach(sentence -> printHeader2("Sentence: " + sentence));
  }

  private boolean notIgnoredElement(final SpatialExpression se) {
    if (!elementsToIgnoreByPos.contains(Nuller.resolve(() -> se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getPos()).orElse(""))
        && !elementsToIgnoreByBase.contains(Nuller.resolve(() -> se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase()).orElse(""))
        && !elementsToIgnoreByPos.contains(Nuller.resolve(() -> se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getPos()).orElse(""))
        && !elementsToIgnoreByBase.contains(Nuller.resolve(() -> se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getBase()).orElse(""))
        && objectPos.contains(Nuller.resolve(() -> se.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getPos()).orElse(""))
        && objectPos.contains(Nuller.resolve(() -> se.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getPos()).orElse(""))) {
      return true;
    } else {
      getLogger().debug("IGNORED: " + se.toString());
      return false;
    }
  }

  private void evaluateSentence(final Sentence sentence) {
    recognizer.findCandidates(sentence).forEach(this::evaluateCandidate);
    evalTotalSemanticFilters.getFalseNegatives().stream()
        .filter(r -> r.getSentence().orElse(null) == sentence)
        .map(r -> formatLogFalseNegative(r, "filtered"))
        .sorted()
        .forEach(System.out::println);
    evalTotalCandidates.getFalseNegatives().stream()
        .filter(r -> r.getSentence().orElse(null) == sentence)
        .map(r -> formatLogFalseNegative(r, "candidate"))
        .sorted()
        .forEach(System.out::println);
  }

  private String formatLogFalseNegative(final SpatialExpression se, final String type) {
    return String.format("[%s] FalseNegative: %s [key=%s] [width=%d]",
        type, se.toString(), keyGenerator.generateKey(se), se.getWidth());
  }

  private void evaluateCandidate(final SpatialExpression candidate) {
    final Optional<String> filterName = recognizer.getFilterDiscardingRelation(candidate);
    final String status = filterName.isPresent() ? "REMOVED by" : "OK    ";
    String eval;

    evalTotalCandidates.addDecision(candidate);
    if (filterName.isPresent()) {
      eval = evalTotalSemanticFilters.containsAsGold(candidate) ? "Filtered-False" : "Filtered-True";
    } else {
      final FscoreEvaluator evalType = evalByTypeTotal.computeIfAbsent(candidate.getType(), k -> new FscoreEvaluator());
      if (evalTotalSemanticFilters.containsAsGold(candidate)) {
        eval = "TruePositive";
        evalType.addTruePositive();
      } else {
        eval = "FalsePositive";
        evalType.addFalsePositive();
      }
      eval += evalTotalSemanticFilters.containsAsDecision(candidate) ? "-DUPLICATE" : "-FIRST";
      evalTotalSemanticFilters.addDecision(candidate);
    }
    logCandidate(candidate, eval, status, filterName);

  }

  private void logCandidate(final SpatialExpression candidate, final String eval, final String status, final Optional<String> filterName) {
    final String concepts = recognizer.getSemanticFilter().match(candidate).stream()
        .map(p -> new StringBuilder(p.getName())
            .append(" (TR subclass of: ")
            .append(subconceptsOfToString(p.getTrajectorConcepts(), candidate.getTrajectorConcepts(), sumo))
            .append("; LM subclass of: ")
            .append(subconceptsOfToString(p.getLandmarkConcepts(), candidate.getLandmarkConcepts(), sumo))
            .append(")").toString())
        .collect(Collectors.joining(" & "));

    String info = String.format(" %-20s %-60s\t%s %-10s Key=%s; schema=%s",
        eval, candidate.toString(), status, filterName.orElse(""), keyGenerator.generateKey(candidate), concepts);
    if (regions.contains(candidate.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase())) {
      info += " REGION_AS_LANDMARK";
    }
    System.out.println(info);
    System.out.println(String.format(" %25s   SI: %s", "", io.vavr.control.Option.of(candidate.getSpatialIndicator()).map(Annotation::getText).getOrElse("")));
    System.out.println(String.format(" %25s   TR: %s", "", candidate.getTrajector() + " => " + String.join(", ", candidate.getTrajectorConcepts())));
    System.out.println(String.format(" %25s   LM: %s", "", candidate.getLandmark() + " => " + String.join(", ", candidate.getLandmarkConcepts())));
    System.out.println();
  }

  private String formatEvalLine(final String type, final FscoreEvaluator eval) {
    return String.format("%-30s %6.2f %6d %6d", type, eval.precision() * 100, eval.getTruePositiveCount(), eval.getFalsePositiveCount());
  }

  private String subconceptsOfToString(final Collection<String> concepts, final Set<String> superConcepts, final Sumo sumo) {
    return concepts.stream()
        .filter(concept -> sumo.isClassOrSubclassOf(superConcepts, concept))
        .collect(Collectors.joining(" "));
  }

}
