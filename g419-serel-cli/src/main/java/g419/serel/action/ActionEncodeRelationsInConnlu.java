package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.serel.ruleTree.PatternMatch;
import g419.serel.structure.patternMatch.PatternMatchSingleResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import java.io.*;
import java.util.*;


@Slf4j
public class ActionEncodeRelationsInConnlu extends Action {

  @Setter
  private String inputFilename;
  @Setter
  private String inputFormat;
  @Setter
  private String outputFilename;

  @Setter
  private String rulesFilename;

  private String reportFilename;

  @Setter
  boolean verbose;


  boolean useEliminateStage = false;


  boolean printSectionFound = false;
  boolean printSectionTruePositive = false;
  boolean printSectionFalsePositive = false;
  boolean printSectionFalseNegative = false;

  private int counter = 0;

  private boolean firstPass = true;

  private final List<String> patterns = new LinkedList<>();
  private final Set<String> patternsSet = new LinkedHashSet<>();

  private final List<List<PatternMatchSingleResult>> patternsResults = new LinkedList<>();
  private final List<List<PatternMatchSingleResult>> patternsResultsTruePositive = new LinkedList<>();
  private final List<List<PatternMatchSingleResult>> patternsResultsFalsePositive = new LinkedList<>();

  List<PatternMatchSingleResult> documentResult = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultTruePositive = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultFalsePositive = new ArrayList<>();

  Map<String, Set<PatternMatchSingleResult>> documentName2documentResult = new HashMap<>();

  Map<String, Map<Integer, List<RelationDesc>>> resultFalseNegativeTotalMap = new HashMap<>();

  private List<PatternMatchSingleResult> sentenceResultsTruePositive;
  private List<PatternMatchSingleResult> sentenceResultsFalsePositive;


  @Getter
  private final List<PatternMatchSingleResult> resultTotal = new LinkedList<>();

  public ActionEncodeRelationsInConnlu() {
    super("encode-in-conllu");
    setDescription("Extracts relations and encodes them in corresponding files ");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getRuleFilenameOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getReportFileNameOption());
    options.addOption(CommonOptions.getPrintSectionsOption());
    options.addOption(CommonOptions.getVerifyRelationsOption());
    options.addOption(CommonOptions.getEliminateStageModeOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    rulesFilename = line.getOptionValue(CommonOptions.OPTION_RULE_FILENAME);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);

    if (line.hasOption(CommonOptions.OPTION_VERBOSE)) {
      verbose = true;
    }

    if (line.hasOption(CommonOptions.OPTION_ELIMINATE_STAGE)) {
      useEliminateStage = true;
    }

    String printSectionsMask = line.getOptionValue(CommonOptions.OPTION_PRINT_SECTION);
    if (printSectionsMask == null) {
      printSectionsMask = "0000";
    }

    printSectionFound = (printSectionsMask.charAt(0) == '1');
    printSectionTruePositive = (printSectionsMask.charAt(1) == '1');
    printSectionFalsePositive = (printSectionsMask.charAt(2) == '1');
    printSectionFalseNegative = (printSectionsMask.charAt(3) == '1');
  }


  @Override
  public void run() throws Exception {

    preprocessParameters();

    patterns.forEach(pattern -> processOnePattern(pattern));

    writeNewVersionsOfConllu();
  }


  private void processOnePattern(final String pattern) {
    System.out.println("Processing pattern " + counter + ": " + pattern);
    counter++;
    //printHeapSize();

    final List<PatternMatchSingleResult> thisPatternResults = new LinkedList<>();
    final List<PatternMatchSingleResult> thisPatternResultsTruePositive = new LinkedList<>();
    final List<PatternMatchSingleResult> thisPatternResultsFalsePositive = new LinkedList<>();

    final PatternMatch patternMatch = PatternMatch.parseRule(pattern);

    if (!isPatternARelation(patternMatch)) {
      System.out.println("ERROR!  Pattern " + pattern + " is not a relation (must contain type). Skipping");
      return;
    }

    try (
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))
    ) {
      reader.forEach(comboedDoc -> {
            try {

              if (firstPass) {
                resultFalseNegativeTotalMap.put(comboedDoc.getName(), new HashMap<>());
                documentName2documentResult.put(comboedDoc.getName(), new HashSet<>());
              }

              matchDocTreeAgainstPatternTree(comboedDoc, patternMatch);

              documentResult.stream().forEach(r -> r.patternMatch = patternMatch);

              thisPatternResults.addAll(documentResult);
              thisPatternResultsTruePositive.addAll(documentResultTruePositive);
              thisPatternResultsFalsePositive.addAll(documentResultFalsePositive);


              documentName2documentResult.get(comboedDoc.getName()).addAll(documentResult);

            } catch (final Exception e) {
              System.out.println("Problem z dokumentem " + comboedDoc.getName());
              e.printStackTrace();
            }
          }
      );
    } catch (
        final Exception e) {
      e.printStackTrace();
    }

    patternsResults.add(thisPatternResults);
    patternsResultsTruePositive.add(thisPatternResultsTruePositive);
    patternsResultsFalsePositive.add(thisPatternResultsFalsePositive);

    this.firstPass = false;
  }

  private void matchDocTreeAgainstPatternTree(final Document d, final PatternMatch patternMatch) {

    documentResult = new ArrayList<>();
    documentResultTruePositive = new ArrayList<>();
    documentResultFalsePositive = new ArrayList<>();

    int sentenceNumber = 0;
    for (final Sentence sentence : d.getParagraphs().get(0).getSentences()) {
      sentenceNumber++;
      try {

        final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceNumber);

//        System.out.println("BEFORE correction");
//        sentence.printAsTree(new PrintWriter(System.out));
        sentence.checkAndFixBois();
//        System.out.println("AFTER correction");
//        sentence.printAsTree(new PrintWriter(System.out));

        sentence.sentenceNumber = sentenceNumber;
        final List<PatternMatchSingleResult> sentenceResults =
            patternMatch.getSentenceTreesMatchingGenericPattern(sentence, useEliminateStage);

        /*
        for (final PatternMatchSingleResult patternMatchSingleResult : sentenceResults) {
          patternMatchSingleResult.sentenceNumber = sentenceNumber;
          patternMatchSingleResult.docName = d.getName();
          // patternMatchSingleResult.patternMatch = patternMatch;
        }
        */

        documentResult.addAll(sentenceResults);

        //classifyResult(sentenceResults, smv, patternMatch);

//        if ((this.sentenceResultsOK.size() > 0) || (this.sentenceResultsNotHit.size() > 0) || (this.sentenceResultsFalseHit.size() > 0)) {
//          System.out.println("PROK=" + sentenceResultsOK);
//          System.out.println("PRFH=" + sentenceResultsFalseHit);
//          System.out.println("PRNH=" + sentenceResultsNotHit);
//        }

        documentResultTruePositive.addAll(sentenceResultsTruePositive);
        documentResultFalsePositive.addAll(sentenceResultsFalsePositive);
      } catch (final Throwable th) {
        th.printStackTrace();
        System.out.println("Problem : " + th);
      }
    } // for
  }

/*
  private void classifyResult(final List<PatternMatchSingleResult> sentenceResults, final SentenceMiscValues smv, final PatternMatch patternMatch) {

    sentenceResultsTruePositive = new LinkedList<>();
    sentenceResultsFalsePositive = new LinkedList<>();

    final List<RelationDesc> allSentenceNamRels = smv.getRelationsMatchingPatternType(patternMatch);

    outer:
    for (final PatternMatchSingleResult pmsr : sentenceResults) {

      for (int i = 0; i < allSentenceNamRels.size(); i++) {
        final RelationDesc rd = allSentenceNamRels.get(i);
        if (pmsr.isTheSameAs(rd)) {
          allSentenceNamRels.remove(i);
          sentenceResultsTruePositive.add(pmsr);
          continue outer;
        }
      }
      sentenceResultsFalsePositive.add(pmsr);
    }
  }
  */

  public void preprocessParameters() throws Exception {

    /*
    try {
      log.info("Wczytywanie Słowosieć 3.2 ...");
      WordnetPl32.load();
      //final WordnetPl wordnetPl = WordnetPl32.load();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    */

    if ((rulesFilename != null) && (!rulesFilename.isEmpty())) {
      System.out.println(" Patterns filename = " + rulesFilename);
      final File file = new File(rulesFilename);
      final BufferedReader br = new BufferedReader(new FileReader(file));

      String pattern;
      boolean commentMode = false;
      while ((pattern = br.readLine()) != null) {
        if (!pattern.isEmpty()) {

          if (pattern.trim().startsWith("*/")) {
            commentMode = false;
            continue;
          }

          if (pattern.trim().startsWith("/*")) {
            commentMode = true;
            continue;
          }

          if (commentMode) {
            continue;
          }

          if (!pattern.trim().startsWith("#") && !(pattern.trim().startsWith("//"))) {
            patternsSet.add(pattern);
          } else {
            System.out.println("Commented out pattern: " + pattern);
          }
        }
      }
    }
//    System.out.println("verify_relations mode  = " + verifyRelationsMode);
//    System.out.println("Number of unique patterns found  = " + patternsSet.size());
    patterns.addAll(patternsSet);
  }


  private boolean isPatternARelation(final PatternMatch pattern) {
    if ((pattern.getRelationType() == null)
        ||
        (pattern.getRelationType().trim().isEmpty())) {
      return false;
    }

    return true;
  }

  private void writeNewVersionsOfConllu() {


    String output_file = "/home/michalolek/NLPWR/corpora/KPWr/targetConll/targetIndex.list";
    String output_format = "batch:conll";


    try (
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))
    ) {


      reader.forEach(comboedDoc -> {
        try {

          Set<PatternMatchSingleResult> pmsrs = documentName2documentResult.get(comboedDoc.getName());

          for (PatternMatchSingleResult pmsr : pmsrs) {

            Sentence sent = comboedDoc.getParagraphs().get(0).getSentences().get(pmsr.sentenceNumber - 1);
            SentenceMiscValues.from(sent, pmsr.sentenceNumber);

            List<Token> tokens = sent.getTokens();

            Token token = tokens.get(pmsr.patternMatchExtraInfo.getRoleE1MinId() - 1);
            token.getNamRels().add(pmsr.getAsRelationDesc());

            token.setAttributeValue("misc", token.extrAttrToString());
            token.setAttributeValue("misc", token.extrAttrToString());
          }

          writer.writeDocument(comboedDoc);

        } catch (final Exception e) {
          System.out.println("2 Problem z dokumentem " + comboedDoc.getName());
          e.printStackTrace();
        }


      });


    } catch (final Exception e) {
      System.out.println("Jakis problemm przy zapisywaniu");
      e.printStackTrace();
    }


  }


}

