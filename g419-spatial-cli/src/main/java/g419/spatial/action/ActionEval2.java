package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.FscoreEvaluator;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.tools.*;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class ActionEval2 extends Action {

    final private List<Pattern> annotationsPrep = Lists.newLinkedList();
    final private List<Pattern> annotationsNg = Lists.newLinkedList();
    final private Set<String> objectPos;
    private String filename = null;
    private String inputFormat = null;
    private String maltparserModel = null;
    private String wordnetPath = null;

    /**
     *
     */
    public ActionEval2() {
        super("eval2");
        this.setDescription("evaluate recognition of spatial expressions (new approach including dynamic)");
        this.options.addOption(this.getOptionInputFilename());
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getMaltparserModelFileOption());
        this.options.addOption(CommonOptions.getWordnetOption(true));

        this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
        this.annotationsNg.add(Pattern.compile("^NG.*"));
        this.objectPos = Sets.newHashSet("subst", "ign", "brev");
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
        maltparserModel = line.getOptionValue(CommonOptions.OPTION_MALT);
        wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
    }

    @Override
    public void run() throws Exception {
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat);
        final Wordnet3 wordnet = new Wordnet3(wordnetPath);
        final MaltParser malt = new MaltParser(maltparserModel);
        final SpatialRelationRecognizer2 recognizer = new SpatialRelationRecognizer2(malt, wordnet);

        final Set<String> regions = SpatialResources.getRegions();

        final KeyGenerator<SpatialExpression> keyGenerator = new SpatialExpressionKeyGeneratorSimple();
        final DecisionCollector<SpatialExpression> evalTotal = new DecisionCollector<>(keyGenerator);
        final DecisionCollector<SpatialExpression> evalNoSeedTotal = new DecisionCollector<>(keyGenerator);

        final Map<String, FscoreEvaluator> evalByTypeTotal = Maps.newHashMap();
        final Sumo sumo = recognizer.getSemanticFilter().getSumo();
        final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

        while (reader.hasNext()) {
            final Document document = reader.nextDocument();
            printHeader1("Document: " + document.getName());
            final List<SpatialExpression> gold = converter.convert(document);
            evalTotal.addAllGold(gold);
            evalNoSeedTotal.addAllGold(gold);
            for (Sentence sentence : document.getSentences()) {
                printHeader2("Sentence: " + sentence);
                List<SpatialExpression> relations = recognizer.findCandidates(sentence);

                gold.stream().filter(r -> r.getLandmark() != null && r.getLandmark().getSpatialObject() != null)
                        .filter(r -> r.getLandmark().getSpatialObject().getSentence() == sentence)
                        .forEach(r -> System.out.println(" Gold: " + r.toString() + " [Key="+keyGenerator.generateKey(r)+"]"));
                System.out.println();

                for (SpatialExpression rel : relations) {
                    Optional<String> filterName = recognizer.getFilterDiscardingRelation(rel);
                    String status = filterName.isPresent() ? "REMOVED by" : "OK    ";
                    String eval;

                    evalNoSeedTotal.addDecision(rel);
                    if (filterName.isPresent()) {
                        eval = evalTotal.containsAsGold(rel) ? "FalseNegative" : "";
                    } else {
                        FscoreEvaluator evalType = evalByTypeTotal.computeIfAbsent(rel.getType(), k -> new FscoreEvaluator());
                        if (evalTotal.containsAsGold(rel)) {
                            eval = "TruePositive";
                            evalType.addTruePositive();
                        } else {
                            eval = "FalsePositive";
                            evalType.addFalsePositive();
                        }
                        eval += evalTotal.containsAsDecision(rel) ? "-DUPLICATE" : "-FIRST";
                        evalTotal.addDecision(rel);
                    }

                    StringBuilder sb = new StringBuilder();
                    for (SpatialRelationSchema p : recognizer.getSemanticFilter().match(rel)) {
                        sb.append(sb.length() > 0 ? " & " : "");
                        sb.append(p.getName());
                        sb.append(" (TR subclass of:");
                        p.getTrajectorConcepts().stream()
                                .filter(tc -> sumo.isClassOrSubclassOf(rel.getTrajectorConcepts(), tc)).forEach(tc -> sb.append(" " + tc));
                        sb.append("; LM subclass of:");
                        p.getLandmarkConcepts().stream()
                                .filter(lm -> sumo.isClassOrSubclassOf(rel.getLandmarkConcepts(), lm)).forEach(lm -> sb.append(" " + lm));
                        sb.append(")");
                    }

                    String info = String.format(" %-20s %-60s\t%s %-10s Key=%s; schema=%s",
                            eval, rel.toString(), status, filterName.orElse(""), keyGenerator.generateKey(rel), sb.toString());
                    if (regions.contains(rel.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase())) {
                        info += " REGION_AS_LANDMARK";
                    }
                    System.out.println(info);
                    System.out.println(String.format(" %25s   SI: %s", "", (rel.getSpatialIndicator() == null ? "" : rel.getSpatialIndicator().getText())));
                    System.out.println(String.format(" %25s   TR: %s", "", rel.getTrajector() + " => " + String.join(", ", rel.getTrajectorConcepts())));
                    System.out.println(String.format(" %25s   LM: %s", "", rel.getLandmark() + " => " + String.join(", ", rel.getLandmarkConcepts())));
                    System.out.println();
                }
            }
        }

        reader.close();
        printHeader1("Z sitem semantycznym");
        evalTotal.getConfusionMatrix().printTotal();
        System.out.println("-----------------------------");
        evalByTypeTotal.entrySet().stream().map(p -> formatEvalLine(p.getKey(), p.getValue())).forEach(System.out::println);
        printHeader1("Bez sita semantycznego");
        evalNoSeedTotal.getConfusionMatrix().printTotal();
    }

    private String formatEvalLine(String type, FscoreEvaluator eval) {
        return String.format("%-20s P=%6.2f TP=%4d FP=%4d", type, eval.precision() * 100, eval.getTruePositiveCount(), eval.getFalsePositiveCount());
    }

    private void printHeader1(String header) {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println(header);
        System.out.println("==========================================================");
    }

    private void printHeader2(String header) {
        System.out.println();
        System.out.println(header);
        System.out.println("----------------------------------------------------------");
    }

}
