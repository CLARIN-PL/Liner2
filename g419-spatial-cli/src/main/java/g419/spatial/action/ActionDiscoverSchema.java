package g419.spatial.action;

import com.google.common.collect.Lists;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.filter.*;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;

public class ActionDiscoverSchema extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private final List<Pattern> annotationsPrep = new LinkedList<>();
  private final List<Pattern> annotationsNg = new LinkedList<>();

  private final Pattern patternAnnotationNam = Pattern.compile("^nam(_.*|$)");

  private String filename = null;
  private String inputFormat = null;

  private final Logger logger = Logger.getLogger("ActionSpatial");
  private final Set<String> objectPos = new HashSet<>();

  Sumo sumo;
  Wordnet3 wordnet;
  NamToWordnet nam2wordnet;
  List<IRelationFilter> filters;
  MaltParser malt;
  RelationFilterSemanticPattern semanticFilter;

  /* Parametry, które będzie trzeba wyciągnąć do pliku ini. */
  private final String config_liner2_model = "liner25-model-pack-ibl/config-n82.ini";
  private final String config_iobber_model = "model-kpwr11-H";
  private final String config_iobber_config = "kpwr.ini";
  private final String wordnetPath = "/nlp/resources/plwordnet/plwordnet_2_3_mod/plwordnet_2_3_pwn_format/";

  public ActionDiscoverSchema() {
    super("discover-schema");
    setDescription("recognize spatial relations");
    options.addOption(getOptionInputFilename());
    options.addOption(CommonOptions.getInputFileFormatOption());

    annotationsPrep.add(Pattern.compile("^PrepNG.*"));
    annotationsNg.add(Pattern.compile("^NG.*"));

    objectPos.add("subst");
    objectPos.add("ign");
    objectPos.add("brev");
  }

  /**
   * Create Option object for input file name.
   *
   * @return Object for input file name parameter.
   */
  private Option getOptionInputFilename() {
    return Option.builder(ActionDiscoverSchema.OPTION_FILENAME).hasArg().argName("FILENAME").required()
        .desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(ActionDiscoverSchema.OPTION_FILENAME);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
  }

  @Override
  public void run() throws Exception {
    sumo = new Sumo();
    wordnet = new Wordnet3(wordnetPath);
    nam2wordnet = new NamToWordnet(wordnet);
    semanticFilter = new RelationFilterSemanticPattern();

    filters = Lists.newArrayList();
    filters.add(new RelationFilterSpatialIndicator());
    filters.add(new RelationFilterPronoun());
    filters.add(semanticFilter);
    filters.add(new RelationFilterPrepositionBeforeLandmark());
    filters.add(new RelationFilterLandmarkTrajectorException());
    filters.add(new RelationFilterHolonyms(wordnet, nam2wordnet));

//    final IobberChunker iobber = new IobberChunker("", config_iobber_model, config_iobber_config);
//    final Liner2 liner2 = new Liner2(config_liner2_model);
//    liner2.chunkInPlace(document);
//    iobber.chunkInPlace(document);

    malt = new MaltParser("/nlp/resources/maltparser/skladnica_liblinear_stackeager_final.mco");

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat)) {
      Document document = null;
      while ((document = reader.nextDocument()) != null) {
        getLogger().info("Document: {}", document.getName());
        processDocument(document);
      }
    }
  }

  private void processDocument(final Document document) throws MaltChainedException {
    for (final Paragraph paragraph : document.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {

        splitPrepNg(sentence);

        final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
        malt.parse(maltSentence);

        /* Zaindeksuj frazy np */
        final Map<Integer, Annotation> chunkNpTokens = new HashMap<>();
        final Map<Integer, Annotation> chunkVerbfinTokens = new HashMap<>();
        final Map<Integer, Annotation> chunkPrepTokens = new HashMap<>();
        final Map<Integer, Annotation> chunkPpasTokens = new HashMap<>();
        final Map<Integer, Annotation> chunkPactTokens = new HashMap<>();
        for (final Annotation an : sentence.getChunks()) {
          if (an.getType().equals("chunk_np")) {
            for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
              chunkNpTokens.put(n, an);
            }
          } else if (an.getType().equals("Prep")) {
            for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
              chunkPrepTokens.put(n, an);
            }
          } else if (an.getType().equals("Pact")) {
            for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
              chunkPactTokens.put(n, an);
            }
          } else if (an.getType().equals("Ppas")) {
            for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
              chunkPpasTokens.put(n, an);
            }
          } else if (an.getType().equals("Verbfin")) {
            for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
              chunkVerbfinTokens.put(n, an);
            }
          }
        }

        /* Zaindeksuj pierwsze tokeny anotacji NG* */
        final Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<>();
        for (final Annotation an : sentence.getAnnotations(annotationsNg)) {
          for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
            if (!mapTokenIdToAnnotations.containsKey(n)) {
              mapTokenIdToAnnotations.put(n, new LinkedList<>());
            }
            mapTokenIdToAnnotations.get(n).add(an);
          }
        }


        final List<SpatialExpression> relations = new LinkedList<>();

        // Second Iteration only

        relations.addAll(findCandidatesFirstNgAnyPrepNg(sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens));
        relations.addAll(findCandidatesByMalt(sentence, maltSentence));


        relations.addAll(findCandidatesNgPpasPrepNg(
            sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPpasTokens, chunkPrepTokens));
        relations.addAll(findCandidatesNgPactPrepNg(
            sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPactTokens, chunkPrepTokens));

        replaceNgWithNames(sentence, relations);

        if (relations.size() > 0) {
          for (final SpatialExpression rel : relations) {

            boolean pass = true;

            for (final IRelationFilter filter : filters) {
              if (!filter.pass(rel)) {
                System.out.println("- " + rel.toString() + "\t" + filter.getClass().getSimpleName());
                if (filter.getClass() == RelationFilterSemanticPattern.class) {
                  final Set<String> trajectorConcepts = rel.getTrajectorConcepts();
                  final Set<String> landmarkConcepts = rel.getLandmarkConcepts();
                  System.out.println("\t\t\tTrajector = " + rel.getTrajector() + " => " + String.join(", ", trajectorConcepts));
                  System.out.println("\t\t\tLandmark  = " + rel.getLandmark() + " => " + String.join(", ", landmarkConcepts));

                  final Set<String> trajectorConceptsSuper = new HashSet<>();
                  final Set<String> landmarkConceptsSuper = new HashSet<>();

                  for (final String concept : trajectorConcepts) {
                    trajectorConceptsSuper.addAll(sumo.getSuperclasses(concept));
                  }

                  for (final String concept : landmarkConcepts) {
                    landmarkConceptsSuper.addAll(sumo.getSuperclasses(concept));
                  }

                  if ((trajectorConceptsSuper.contains("physical")
                      || trajectorConceptsSuper.contains("object")) && landmarkConceptsSuper.contains("physical")) {
                    System.out.println("PHYSICAL/OBJECT");
                  }
                }
                pass = false;
                break;
              }
            }

            if (pass) {
              final StringBuilder sb = new StringBuilder();
              for (final SpatialRelationSchema p : semanticFilter.match(rel)) {
                if (sb.length() > 0) {
                  sb.append(" & ");
                }
                sb.append(p.getName());
              }

              System.out.println("+ " + rel.toString());
            }
          }

        }
      }
    }
  }

  /**
   * Dla fraz NG, które pokrywają się z nazwą własną, zmienia NG na nazwę.
   *
   * @param sentence
   * @param relations
   */
  private void replaceNgWithNames(final Sentence sentence, final List<SpatialExpression> relations) {
    final Map<String, Annotation> names = new HashMap<>();
    for (final Annotation an : sentence.getAnnotations(patternAnnotationNam)) {
      final String key = String.format("%d:%d", an.getBegin(), an.getEnd());
      if (names.containsKey(key)) {
        Logger.getLogger(getClass()).warn(String.format("Name for key '%s' already exists: %s", key, an));
      } else {
        names.put(key, an);
      }
    }
    for (final SpatialExpression relation : relations) {
      // Sprawdź landmark
      final String landmarkKey = String.format("%d:%d", relation.getLandmark().getSpatialObject().getBegin(), relation.getLandmark().getSpatialObject().getEnd());
      final Annotation landmarkName = names.get(landmarkKey);
      if (landmarkName != null) {
        Logger.getLogger(getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getLandmark().getSpatialObject().getType(), relation.getLandmark(), landmarkName));
        landmarkName.setHead(relation.getLandmark().getSpatialObject().getHead());
        relation.setLandmark(landmarkName);
      }
      // Sprawdź trajector
      final String trajectorKey = String.format("%d:%d", relation.getTrajector().getSpatialObject().getBegin(), relation.getTrajector().getSpatialObject().getEnd());
      final Annotation trajectorName = names.get(trajectorKey);
      if (trajectorName != null) {
        Logger.getLogger(getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getTrajector().getSpatialObject().getType(), relation.getTrajector(), trajectorName));
        relation.setTrajector(trajectorName);
      }
    }
  }

  /**
   * Wydziela z anotacji PrepNG* anotacje zagnieżdżone poprzez odcięcie przymika.
   *
   * @param sentence
   */
  public void splitPrepNg(final Sentence sentence) {
    /* Zaindeksuj pierwsze tokeny anotacji NG* */
    final Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<>();
    for (final Annotation an : sentence.getAnnotations(annotationsNg)) {
      if (!mapTokenIdToAnnotations.containsKey(an.getBegin())) {
        mapTokenIdToAnnotations.put(an.getBegin(), new LinkedList<>());
      }
      mapTokenIdToAnnotations.get(an.getBegin()).add(an);
    }

    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      if (!mapTokenIdToAnnotations.containsKey(an.getBegin() + 1)) {
        final Annotation ani = new Annotation(an.getBegin() + 1, an.getEnd(), an.getType().substring(4), an.getSentence());
        ani.setHead(an.getHead());
        sentence.addChunk(ani);
      }
    }
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgAnyPrepNg(final Sentence sentence,
                                                           final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                           final Map<Integer, Annotation> chunkNpTokens,
                                                           final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      Integer trajectorPrevId = an.getBegin() - 1;

      boolean breakSearchPrev = false;
      while (!breakSearchPrev && trajectorPrevId >= 0 && mapTokenIdToAnnotations.get(trajectorPrevId) == null) {
        /* Przecinek i nawias zamykający przerywają poszykiwanie */
        final String orth = sentence.getTokens().get(trajectorPrevId).getOrth();
        if (orth.equals(",") || orth.equals(")")) {
          breakSearchPrev = true;
        }
        trajectorPrevId--;
      }
      final Integer trajectorId = trajectorPrevId;

      if (chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorPrevId) == chunkNpTokens.get(landmarkId)) {
        String type = "";
        if (trajectorPrevId + 1 == preposition.getBegin()) {
          type = "<NG|PrepNG>";
        } else {
          type = "<NG|...|PrepNG>";
        }
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPrepNgNoNp(final Sentence sentence,
                                                            final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                            final Map<Integer, Annotation> chunkNpTokens,
                                                            final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer trajectorId = an.getBegin() - 1;

      if (chunkNpTokens.get(landmarkId) == null
          && chunkNpTokens.get(trajectorId) == null) {
        final String type = "NG|PrepNG";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPrepNgDiffNp(final Sentence sentence,
                                                              final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                              final Map<Integer, Annotation> chunkNpTokens,
                                                              final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer trajectorId = an.getBegin() - 1;

      if (chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(trajectorId) != null
          && chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId)) {
        final String type = "<NG><PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesPrepNgNgDiffNp(final Sentence sentence,
                                                              final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                              final Map<Integer, Annotation> chunkNpTokens,
                                                              final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer trajectorId = an.getEnd() + 1;

      if (chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(trajectorId) != null
          && chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId)) {
        final String type = "<PrepNG><NG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPrepNgPpasPrepNg(final Sentence sentence,
                                                                  final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                  final Map<Integer, Annotation> chunkNpTokens,
                                                                  final Map<Integer, Annotation> chunkPrepTokens,
                                                                  final Map<Integer, Annotation> chunkPpasTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      if (an.getBegin() - 1 <= 0 || !sentence.getTokens().get(an.getBegin() - 1).getOrth().equals(",")) {
        continue;
      }

      final Annotation ppas = chunkPpasTokens.get(an.getBegin() - 1);

      if (ppas == null) {
        continue;
      }

      final List<Annotation> ngs = mapTokenIdToAnnotations.get(ppas.getBegin() - 1);
      if (ngs == null) {
        continue;
      }

      final Annotation prep = chunkPrepTokens.get(ngs.get(0).getBegin() - 1);
      if (prep == null) {
        continue;
      }

      final Integer trajectorId = prep.getBegin() - 1;

      if (chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
        final String type = "<NG|PrepNG|Ppas|PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPrepNgCommaPrepNg(final Sentence sentence,
                                                                   final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                   final Map<Integer, Annotation> chunkNpTokens,
                                                                   final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer commaId = preposition.getBegin() - 1;

      if (commaId <= 0 || !sentence.getTokens().get(commaId).getOrth().equals(",")) {
        continue;
      }

      Integer prepId = null;
      final List<Annotation> ngs = mapTokenIdToAnnotations.get(commaId - 1);
      if (ngs == null) {
        continue;
      } else {
        for (final Annotation a : ngs) {
          if (prepId == null) {
            prepId = a.getBegin() - 1;
          } else {
            prepId = Math.min(prepId, a.getBegin() - 1);
          }
        }
      }

      final Annotation prep = chunkPrepTokens.get(prepId);
      if (prep == null) {
        continue;
      }

      final Integer trajectorId = prep.getBegin() - 1;

      if (chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
        final String type = "<NG|PrepNG|Comma|PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPrepNgPrepNg(final Sentence sentence,
                                                              final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                              final Map<Integer, Annotation> chunkNpTokens,
                                                              final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();

      final List<Annotation> ngs = mapTokenIdToAnnotations.get(an.getBegin() - 1);
      Integer prepId = null;
      if (ngs == null) {
        continue;
      } else {
        for (final Annotation a : ngs) {
          if (prepId == null) {
            prepId = a.getBegin() - 1;
          } else {
            prepId = Math.min(prepId, a.getBegin() - 1);
          }
        }
      }

      final Annotation prep = chunkPrepTokens.get(prepId);
      if (prep == null) {
        continue;
      }

      final Integer trajectorId = prep.getBegin() - 1;

      if (chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
        final String type = "<NG|PrepNG|PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgNgPrepNg(final Sentence sentence,
                                                          final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                          final Map<Integer, Annotation> chunkNpTokens,
                                                          final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();

      final List<Annotation> ngs = mapTokenIdToAnnotations.get(preposition.getBegin() - 1);
      Integer trajectorId = null;
      if (ngs == null) {
        continue;
      } else {
        for (final Annotation a : ngs) {
          if (trajectorId == null) {
            trajectorId = a.getBegin() - 1;
          } else {
            trajectorId = Math.min(trajectorId, a.getBegin() - 1);
          }
        }
      }

      if (chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
        final String type = "<NG|NG|PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesFirstNgAnyPrepNg(final Sentence sentence,
                                                                final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                final Map<Integer, Annotation> chunkNpTokens,
                                                                final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców NG* prep NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      Integer trajectorId = an.getBegin() - 1;

      boolean breakSearchPrev = false;
      while (!breakSearchPrev && trajectorId >= 0 && mapTokenIdToAnnotations.get(trajectorId) == null) {
        /* Przecinek i nawias zamykający przerywają poszykiwanie */
        final String orth = sentence.getTokens().get(trajectorId).getOrth();
        if (orth.equals(",") || orth.equals(")")) {
          breakSearchPrev = true;
        }
        trajectorId--;
      }

      if (chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {

        while (trajectorId > 0
            && mapTokenIdToAnnotations.get(trajectorId - 1) != null
            && chunkNpTokens.get(trajectorId - 1) == chunkNpTokens.get(landmarkId)
        ) {
          trajectorId = mapTokenIdToAnnotations.get(trajectorId - 1).get(0).getBegin();
        }

        String type = "";
        if (trajectorId + 1 == preposition.getBegin()) {
          type = "<FirstNG|PrepNG>";
        } else {
          type = "<FirstNG|...|PrepNG>";
        }

        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesPrepNgNg(final Sentence sentence,
                                                        final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                        final Map<Integer, Annotation> chunkNpTokens,
                                                        final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców prep NG* NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer trajectorId = an.getEnd() + 1;

      if (chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
        final String type = "<PrepNG|NG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPrepNg(final Sentence sentence,
                                                        final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                        final Map<Integer, Annotation> chunkNpTokens,
                                                        final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców prep NG* NG* */
    for (final Annotation landmark : sentence.getAnnotations(annotationsNg)) {

      final Integer prepId = landmark.getBegin() - 1;
      if (prepId <= 0
          || !sentence.getTokens().get(prepId).getDisambTag().equals("prep")
          || chunkPrepTokens.get(prepId) != null) {
        continue;
      }

      final Integer landmarkId = landmark.getBegin();
      final Integer trajectorId = prepId - 1;

      if (chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
        final String type = "<NG|prep|NG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks,
            new Annotation(prepId, "Prep", sentence)));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesPrepNgVerbfinNg(
      final Sentence sentence,
      final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
      final Map<Integer, Annotation> chunkVerbfinTokens,
      final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców prep NG* NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer verbfinId = an.getEnd() + 1;
      final Annotation verbfin = chunkVerbfinTokens.get(verbfinId);

      if (verbfin != null) {
        final Integer trajectorId = verbfin.getEnd() + 1;
        final String type = "PrepNG|Verbfin|NG";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgVerbfinPrepNg(final Sentence sentence,
                                                               final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                               final Map<Integer, Annotation> chunkVerbfinTokens,
                                                               final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców prep NG* NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer verbfinId = preposition.getBegin() - 1;
      final Annotation verbfin = chunkVerbfinTokens.get(verbfinId);

      if (verbfin != null) {
        final Integer trajectorId = verbfin.getBegin() - 1;
        final String type = "NG|Verbfin|PrepNG";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPpasPrepNg(final Sentence sentence,
                                                            final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                            final Map<Integer, Annotation> chunkNpTokens,
                                                            final Map<Integer, Annotation> chunkPpasTokens,
                                                            final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców prep NG* NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer verbfinId = preposition.getBegin() - 1;
      final Annotation ppas = chunkPpasTokens.get(verbfinId);
      if (ppas == null) {
        continue;
      }
      final Integer trajectorId = ppas.getBegin() - 1;

      if (ppas != null && chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
        final String type = "<NG|Ppas|PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
   *
   * @param sentence
   */
  public List<SpatialExpression> findCandidatesNgPactPrepNg(final Sentence sentence,
                                                            final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                            final Map<Integer, Annotation> chunkNpTokens,
                                                            final Map<Integer, Annotation> chunkPactTokens,
                                                            final Map<Integer, Annotation> chunkPrepTokens) {
    final List<SpatialExpression> relations = new LinkedList<>();
    /* Szukaj wzorców prep NG* NG* */
    for (final Annotation an : sentence.getAnnotations(annotationsPrep)) {
      final Annotation preposition = chunkPrepTokens.get(an.getBegin());
      if (preposition == null) {
        logger.warn("Prep annotation for PrepNG not found: " + an.toString());
        continue;
      }

      final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
      final Integer verbfinId = preposition.getBegin() - 1;
      final Annotation pact = chunkPactTokens.get(verbfinId);
      if (pact == null) {
        continue;
      }
      final Integer trajectorId = pact.getBegin() - 1;

      if (pact != null && chunkNpTokens.get(landmarkId) != null
          && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
        final String type = "<NG|Pact|PrepNG>";
        final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
        final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
        if (trajectors != null) {
          relations.addAll(generateAllCombinations(type, trajectors, landmarks, preposition));
        }
      }
    }
    return relations;
  }

  /**
   * @param sentence
   * @param maltSentence
   * @return
   */
  public List<SpatialExpression> findCandidatesByMalt(final Sentence sentence, final MaltSentence maltSentence) {
    final List<SpatialExpression> srs = new ArrayList<>();
    for (int i = 0; i < sentence.getTokens().size(); i++) {
      final Token token = sentence.getTokens().get(i);
      final List<Integer> landmarks = new ArrayList<>();
      final List<Integer> trajectors = new ArrayList<>();
      Integer indicator = null;
      final String type = "MALT";
      String typeLM = "";
      String typeTR = "";
      if (ClassFeature.BROAD_CLASSES.get("verb").contains(token.getDisambTag().getPos())) {
        final List<MaltSentenceLink> links = maltSentence.getLinksByTargetIndex(i);
        for (final MaltSentenceLink link : links) {
          final Token tokenChild = sentence.getTokens().get(link.getSourceIndex());
          if (link.getRelationType().equals("subj")) {
            if (tokenChild.getDisambTag().getBase().equals("i")
                || tokenChild.getDisambTag().getBase().equals("oraz")) {
              typeTR = "_TRconj";
              for (final MaltSentenceLink trLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())) {
                landmarks.add(trLink.getSourceIndex());
              }
            } else if (objectPos.contains(tokenChild.getDisambTag().getPos())) {
              trajectors.add(link.getSourceIndex());
            }
          } else if (tokenChild.getDisambTag().getPos().equals("prep")) {
            indicator = link.getSourceIndex();
            for (final MaltSentenceLink prepLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())) {
              final Token lm = sentence.getTokens().get(prepLink.getSourceIndex());
              if (lm.getOrth().equals(",")) {
                typeLM = "_LMconj";
                for (final MaltSentenceLink prepLinkComma : maltSentence.getLinksByTargetIndex(prepLink.getSourceIndex())) {
                  landmarks.add(prepLinkComma.getSourceIndex());
                }
              } else if (objectPos.contains(lm.getDisambTag().getPos())) {
                landmarks.add(prepLink.getSourceIndex());
              }
            }
          }
        }
      }

      if (landmarks.size() > 0 && trajectors.size() > 0 && indicator != null) {
        for (final Integer landmark : landmarks) {
          for (final Integer trajector : trajectors) {
            final SpatialExpression sr = new SpatialExpression(
                type + typeLM + typeTR,
                new Annotation(trajector, "TR", sentence),
                new Annotation(indicator, "SI", sentence),
                new Annotation(landmark, "LM", sentence));
            srs.add(sr);
          }
        }
      }
    }
    return srs;
  }

  /**
   * @param type
   * @param trajectors
   * @param landmarks
   * @param preposition
   * @return
   */
  private List<SpatialExpression> generateAllCombinations(
      final String type, final List<Annotation> trajectors,
      final List<Annotation> landmarks, final Annotation preposition) {
    final List<SpatialExpression> relations = new ArrayList<>();
    if (trajectors != null && landmarks != null) {
      for (final Annotation trajector : trajectors) {
        for (final Annotation landmark : landmarks) {
          final SpatialExpression sr = new SpatialExpression(type, trajector, preposition, landmark);
          relations.add(sr);
        }
      }
    }
    return relations;
  }

}
