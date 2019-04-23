package g419.spatial.action;

import com.google.common.collect.Maps;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.FscoreEvaluator;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.tools.*;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionEval extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private final List<Pattern> annotationsPrep = new LinkedList<>();
  private final List<Pattern> annotationsNg = new LinkedList<>();

  private String filename = null;
  private String inputFormat = null;

  private final Set<String> objectPos = new HashSet<>();

  private String maltparserModel = null;
  private String wordnetPath = null;

  /**
   *
   */
  public ActionEval() {
    super("eval");
    this.setDescription("evaluate recognition of spatial expressions (only for static)");
    this.options.addOption(this.getOptionInputFilename());
    this.options.addOption(CommonOptions.getInputFileFormatOption());
    this.options.addOption(CommonOptions.getMaltparserModelFileOption());
    this.options.addOption(CommonOptions.getWordnetOption(true));

    this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
    this.annotationsNg.add(Pattern.compile("^NG.*"));

    this.objectPos.add("subst");
    this.objectPos.add("ign");
    this.objectPos.add("brev");
  }

  /**
   * Create Option object for input file name.
   *
   * @return Object for input file name parameter.
   */
  private Option getOptionInputFilename() {
    return Option.builder(ActionEval.OPTION_FILENAME).hasArg().argName("FILENAME").required()
        .desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();
  }

  /**
   * Parse action options
   *
   * @param line The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.filename = line.getOptionValue(ActionEval.OPTION_FILENAME);
    this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    this.maltparserModel = line.getOptionValue(CommonOptions.OPTION_MALT);
    this.wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
  }

  @Override
  public void run() throws Exception {
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
    final Wordnet3 wordnet = new Wordnet3(this.wordnetPath);
    final MaltParser malt = new MaltParser(this.maltparserModel);
    final ISpatialRelationRecognizer recognizer = new SpatialRelationRecognizer(malt, wordnet);
    final Set<String> regions = SpatialResources.getRegions();

    //KeyGenerator<SpatialExpression> keyGenerator = new SpatialExpressionKeyGeneratorSimple();
    final KeyGenerator<SpatialExpression> keyGenerator = new SpatialExpressionKeyGeneratorSpatialIndicator();
    final DecisionCollector<SpatialExpression> evalTotal = new DecisionCollector<>(keyGenerator);
    final DecisionCollector<SpatialExpression> evalNoSeedTotal = new DecisionCollector<>(keyGenerator);

    final Map<String, FscoreEvaluator> evalByTypeTotal = Maps.newHashMap();
    final Sumo sumo = new Sumo();

    while (reader.hasNext()) {
      final Document document = reader.next();
      printHeader1("Document: " + document.getName());

      final List<SpatialExpression> gold = this.getSpatialRelations(document);

      if (gold.size() == 0) {
        continue;
      }

      for (final SpatialExpression relation : gold) {
        evalTotal.addGold(relation);
        evalNoSeedTotal.addGold(relation);
      }

      for (final Paragraph paragraph : document.getParagraphs()) {
        for (final Sentence sentence : paragraph.getSentences()) {

          final List<SpatialExpression> relations = recognizer.findCandidates(sentence);

          if (relations.size() > 0) {
            printHeader2("Sentence: " + sentence + "\n");

            for (final SpatialExpression relation : gold) {
              if (relation.getLandmark().getSpatialObject().getSentence() == sentence) {
                System.out.println(relation.toString() + " " + keyGenerator.generateKey(relation));
              }
            }
            System.out.println();

            for (final SpatialExpression rel : relations) {
              String status = "OK    ";
              String filterName = "";
              String eval = "";
              final String duplicate;

              for (final IRelationFilter filter : recognizer.getFilters()) {
                if (!filter.pass(rel)) {
                  status = "REMOVE";
                  filterName = filter.getClass().getSimpleName();
                  break;
                }
              }

              duplicate = evalTotal.containsAsDecision(rel) ? "-DUPLICATE" : "-FIRST";

              evalNoSeedTotal.addDecision(rel);
              if (status.equals("REMOVE")) {
                if (evalTotal.containsAsGold(rel)) {
                  eval = "FalseNegative";
                }
              } else {
                FscoreEvaluator evalType = evalByTypeTotal.get(rel.getType());
                if (evalType == null) {
                  evalType = new FscoreEvaluator();
                  evalByTypeTotal.put(rel.getType(), evalType);
                }

                if (evalTotal.containsAsGold(rel)) {
                  evalType.addTruePositive();
                  eval = "TruePositive";
                } else {
                  evalType.addFalsePositive();
                  eval = "FalsePositive";
                }
                evalTotal.addDecision(rel);
              }

              eval += duplicate;

              final StringBuilder sb = new StringBuilder();
              for (final SpatialRelationSchema p : recognizer.getSemanticFilter().match(rel)) {
                if (sb.length() > 0) {
                  sb.append(" & ");
                }

                sb.append(p.getName());
                sb.append(" (TR subclass of:");
                for (final String concept : p.getTrajectorConcepts()) {
                  if (recognizer.getSemanticFilter().getSumo().isClassOrSubclassOf(rel.getTrajectorConcepts(), concept)) {
                    sb.append(" " + concept);
                  }
                }

                sb.append("; LM subclass of:");
                sb.append(subconceptsOfToString(p.getLandmarkConcepts(), rel.getLandmarkConcepts(), sumo));
                for (final String concept : p.getLandmarkConcepts()) {
                  if (recognizer.getSemanticFilter().getSumo().isClassOrSubclassOf(rel.getLandmarkConcepts(), concept)) {
                    sb.append(" " + concept);
                  }
                }
                sb.append(")");

              }

              String info = String.format("  - %s\t %-80s\t%s %s; id=%s; schema=%s", status, rel.toString(), filterName, eval, keyGenerator.generateKey(rel), sb.toString());
              if (regions.contains(rel.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase())) {
                info += " REGION_AS_LANDMARK";
              }
              System.out.println(info);

              System.out.println("\t\t\tTrajector = " + rel.getTrajector() + " => " + String.join(", ", rel.getTrajectorConcepts()));
              System.out.println("\t\t\tLandmark  = " + rel.getLandmark() + " => " + String.join(", ", rel.getLandmarkConcepts()));
              System.out.println();
            }

          }
        }
      }
    }

    reader.close();
    printHeader1("Z sitem semantycznym");
    evalTotal.getConfusionMatrix().printTotal();
    System.out.println("-----------------------------");

    for (final String type : evalByTypeTotal.keySet()) {
      final FscoreEvaluator evalType = evalByTypeTotal.get(type);
      System.out.println(String.format("%-20s P=%5.2f TP=%d FP=%d", type, evalType.precision() * 100, evalType.getTruePositiveCount(), evalType.getFalsePositiveCount()));
    }

    printHeader1("Bez sita semantycznego");
    evalNoSeedTotal.getConfusionMatrix().printTotal();
  }


  private String subconceptsOfToString(final Collection<String> concepts, final Set<String> superConcepts, final Sumo sumo) {
    return concepts.stream()
        .filter(concept -> sumo.isClassOrSubclassOf(superConcepts, concept))
        .collect(Collectors.joining(" "));
  }

  /**
   * @param document
   * @return
   */
  private List<SpatialExpression> getSpatialRelations(final Document document) {

    final List<SpatialExpression> srs = new ArrayList<>();
    final Map<Annotation, Annotation> landmarks = new HashMap<>();
    final Map<Annotation, List<Annotation>> trajectors = new HashMap<>();
    final Map<Annotation, Annotation> regions = new HashMap<>();
    for (final Relation r : document.getRelations().getRelations()) {
      if (r.getType().equals("landmark")) {
        landmarks.put(r.getAnnotationFrom(), r.getAnnotationTo());
      } else if (r.getType().equals("trajector")) {
        List<Annotation> annotations = trajectors.get(r.getAnnotationFrom());
        if (annotations == null) {
          annotations = new ArrayList<>();
          trajectors.put(r.getAnnotationFrom(), annotations);
        }
        annotations.add(r.getAnnotationTo());
      } else if (r.getType().equals("other") && r.getAnnotationTo().getType().equals("region")) {
        regions.put(r.getAnnotationFrom(), r.getAnnotationTo());
      }
    }
    final Set<Annotation> allIndicators = new HashSet<>();
    allIndicators.addAll(landmarks.keySet());
    allIndicators.addAll(trajectors.keySet());

    for (final Annotation indicator : allIndicators) {
      final Annotation landmark = landmarks.get(indicator);
      final List<Annotation> trajector = trajectors.get(indicator);
      final Annotation region = regions.get(indicator);
      //if ( (landmark != null || region != null) && trajector != null ){
      if (landmark != null && trajector != null) {
//				if ( landmark == null || (landmark != null && region != null && region.getBegin() < landmark.getBegin() ) ){
//					landmark = region;
//				}
        for (final Annotation tr : trajector) {
          // Zignoruje relacje, w któryj trajector lub landmark nie są substem lub ignem
          if (objectPos.contains(tr.getHeadToken().getDisambTag().getPos())
              && objectPos.contains(landmark.getHeadToken().getDisambTag().getPos())) {
            srs.add(new SpatialExpression("Gold", tr, indicator, landmark));
          }
        }
      } else {
        if (landmark == null) {
          getLogger().warn(String.format("Missing landmark for spatial indicator %s", indicator.toString()));
        }
        if (trajector == null) {
          getLogger().warn(String.format("Missing trajector for spatial indicator %s", indicator.toString()));
        }

      }
    }
    return srs;
  }
}
