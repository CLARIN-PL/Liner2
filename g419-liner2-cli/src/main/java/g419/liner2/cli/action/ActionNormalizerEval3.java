package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluation of the normalisation of temporal expressions.
 *
 * @author Jan Kocoń
 */
public class ActionNormalizerEval3 extends Action {

  private String input_file = null;
  private String input_format = null;
  private String point_from = null;
  private String point_what = null;
  private String point_how = null;

  public static final String OPTION_CONFIGURATION = "c";
  public static final String OPTION_CONFIGURATION_LONG = "configuration";

  public ActionNormalizerEval3() {
    super("normalizer-eval3");
    setDescription("processes data with given model");

    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getFeaturesOption());
    options.addOption(CommonOptions.getModelFileOption());
    options.addOption(Option.builder(OPTION_CONFIGURATION)
        .longOpt(OPTION_CONFIGURATION_LONG)
        .hasArg().argName("POINT:WHAT:HOW")
        .desc("WHAT will be compared and from what POINT, e.g.: TEXT:ANN:STRICT, " +
            "TEXT:VAL:RELAXED, ANN:LVAL:RELAXED, LVAL:VAL:RELAXED").build());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
    final String[] configuration = line.getOptionValue(OPTION_CONFIGURATION).split(":");
    point_from = configuration[0];
    point_what = configuration[1];
    point_how = configuration[2];


  }

  /**
   * Module entry function.
   */
  public ArrayList<Document> read_documents() throws Exception {
    final ArrayList<Document> outputList = new ArrayList<>();


    final AbstractDocumentReader reader = getInputReader();


    Document ps = reader.nextDocument();

    while (ps != null) {
      outputList.add(ps);
      ps = reader.nextDocument();
    }
    reader.close();

    return outputList;
  }

  @Override
  public void run() throws Exception {
    //todo: remove these two lines
    Logger.getRootLogger().removeAllAppenders();
    Logger.getRootLogger().addAppender(new NullAppender());

    if (!LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER)) {
      throw new ParameterException("Parameter 'chunker' in 'main' section of model not set");
    }

    TokenFeatureGenerator gen = null;

    if (!LinerOptions.getGlobal().features.isEmpty()) {
      gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
    }

    /* Create all defined chunkers. */
    final ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
    cm.loadChunkers();

    final Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

    final ArrayList<Document> documents = read_documents();

    double intersectionSize = 0, systemSize = 0, referenceSize = 0;
    for (final Document referenceDocument : documents) {
      //System.out.println("Document: " + referenceDocument.getName());
      gen.generateFeatures(referenceDocument);
      chunker.prepare(referenceDocument);
      final Document cloneDocument = referenceDocument.clone();

      if (point_from.equals("TEXT")) {
        cloneDocument.removeAnnotations();
      } else if (point_from.equals("ANN")) {
        cloneDocument.removeMetadata("lval");
        cloneDocument.removeMetadata("val");
      } else if (point_from.equals("LVAL")) {
        cloneDocument.removeMetadata("val");
      }

      final Set<String> typeSet = new HashSet<>(Arrays.asList("t3_date", "t3_time", "t3_duration"));
      chunker.chunkInPlace(cloneDocument);

      final ArrayList<Sentence> referenceSentences = referenceDocument.getSentences();
      final ArrayList<Sentence> cloneSentences = cloneDocument.getSentences();

      for (int i = 0; i < referenceSentences.size(); i++) {
        final HashSet<Annotation> referenceAnnotationSet = referenceSentences.get(i).getChunks().stream()
            .filter(p -> typeSet.contains(p.getType()))
            .collect(Collectors.toCollection(HashSet::new));
        final HashSet<Annotation> systemAnnotationSet = cloneSentences.get(i).getChunks().stream()
            .filter(p -> typeSet.contains(p.getType()))
            .collect(Collectors.toCollection(HashSet::new));

        referenceSize += referenceAnnotationSet.size();
        systemSize += systemAnnotationSet.size();


        if (point_what.equals("ANN") && point_how.equals("STRICT")) {
          //(1) How many entities are correctly identified
          final HashSet<Annotation> intersectionSet = new HashSet<>(systemAnnotationSet);
          intersectionSet.retainAll(referenceAnnotationSet);
          intersectionSize += intersectionSet.size();
        }


        for (final Annotation referenceAnnotation : referenceAnnotationSet) {
          for (final Annotation systemAnnotation : systemAnnotationSet) {
            if (point_how.equals("RELAXED")) {
              if (point_what.equals("ANN")) {
                // (2) If the extents for the entities are correctly identified
                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                    referenceAnnotation.getTokens().stream()
                        .filter(p -> systemAnnotation.getTokens().contains(p))
                        .collect(Collectors.toSet()).size() > 0) {
                  intersectionSize += 1;
                }
              } else if (point_what.equals("LVAL")) {
                // (3) How many entity attributes are correctly identified - LVAL
                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                    referenceAnnotation.getTokens().stream()
                        .filter(p -> systemAnnotation.getTokens().contains(p))
                        .collect(Collectors.toSet()).size() > 0 &&
                    referenceAnnotation.metaDataMatchesKey("lval", systemAnnotation)) {
                  intersectionSize += 1;
                }
                //log!
                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                    referenceAnnotation.getTokens().stream()
                        .filter(p -> systemAnnotation.getTokens().contains(p))
                        .collect(Collectors.toSet()).size() > 0) {
                  if (!referenceAnnotation.metaDataMatchesKey("lval", systemAnnotation)) {
                    System.out.println(
                        referenceAnnotation.getType() + "\t" +
                            referenceDocument.getName() + "\t" +
                            referenceAnnotation.toString() + "\t|" +
                            referenceAnnotation.getBaseText(false) + "|\t" +
                            referenceAnnotation.getMetadata().get("lval") + "\t" +
                            systemAnnotation.getMetadata().get("lval") + "\t" +
                            referenceAnnotation.metaDataMatchesKey("lval", systemAnnotation)
                    );
                  }
                }

              } else if (point_what.equals("VAL")) {
                // (3) How many entity attributes are correctly identified - VAL
                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                    referenceAnnotation.getTokens().stream()
                        .filter(p -> systemAnnotation.getTokens().contains(p))
                        .collect(Collectors.toSet()).size() > 0 &&
                    referenceAnnotation.metaDataMatchesKey("val", systemAnnotation)) {
                  intersectionSize += 1;
                }
                //log!
                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                    referenceAnnotation.getTokens().stream()
                        .filter(p -> systemAnnotation.getTokens().contains(p))
                        .collect(Collectors.toSet()).size() > 0) {
                  if (!referenceAnnotation.metaDataMatchesKey("val", systemAnnotation)) {
                    System.out.println(
                        referenceAnnotation.getType() + "\t" +
                            referenceDocument.getName() + "\t" +
                            referenceAnnotation.toString() + "\t" +
                            referenceAnnotation.getBaseText(false) + "\t" +
                            referenceAnnotation.getMetadata().get("lval") + "\t" +
                            referenceAnnotation.getMetadata().get("val") + "\t" +
                            systemAnnotation.getMetadata().get("val") + "\t" +
                            referenceAnnotation.metaDataMatchesKey("val", systemAnnotation)
                    );
                  }
                }
              }
            }


          }
        }


      }
    }
    final double precision = intersectionSize / systemSize;
    final double recall = intersectionSize / referenceSize;
    final double fmeasure = 2 * precision * recall / (precision + recall);
    final int o = 0;
    final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    System.out.println(format.format(precision * 100));
    System.out.println(format.format(recall * 100));
    System.out.println(format.format(fmeasure * 100));

    System.out.println(precision + " " + recall + " " + fmeasure);
    System.out.format("%,5f", precision);

  }

  /**
   * Get document reader defined with the -i and -f options.
   *
   * @return
   * @throws Exception
   */
  protected AbstractDocumentReader getInputReader() throws Exception {
    return ReaderFactory.get().getStreamReader(input_file, input_format);
  }

}
