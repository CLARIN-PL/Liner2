package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.serel.ruleTree.PatternMatch;
import g419.serel.structure.SentenceMiscValues;
import g419.serel.structure.patternMatch.PatternMatchSingleResult;
import g419.serel.wordnet.WordnetPl32;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@Slf4j
public class ActionMatchRelationsSet extends Action {

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
  boolean verifyRelationsMode;

  @Setter
  boolean verbose;

  private final List<String> patterns = new LinkedList<>();

  private final List<List<PatternMatchSingleResult>> patternsResults = new LinkedList<>();
  private final List<List<PatternMatchSingleResult>> patternsResultsTruePositive = new LinkedList<>();
  private final List<List<PatternMatchSingleResult>> patternsResultsFalsePositive = new LinkedList<>();
  private final List<List<RelationDesc>> patternsResultsFalseNegative = new LinkedList<>();

  List<PatternMatchSingleResult> documentResult = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultTruePositive = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultFalsePositive = new ArrayList<>();
  List<RelationDesc> documentResultFalseNegative = new ArrayList<>();


  private List<PatternMatchSingleResult> sentenceResultsTruePositive;
  private List<PatternMatchSingleResult> sentenceResultsFalsePositive;
  private List<RelationDesc> sentenceResultsFalseNegative;


  List<PatternMatchSingleResult> resultOK = new ArrayList<>();
  List<PatternMatchSingleResult> resultFalseHit = new ArrayList<>();
  List<RelationDesc> resultFalseNegative = new ArrayList<>();


  @Getter
  //private List<PatternMatchSingleResult> result;
  private final List<PatternMatchSingleResult> resultTotal = new LinkedList<>();
  private final List<PatternMatchSingleResult> resultTruePositiveTotal = new LinkedList<>();
  private final List<PatternMatchSingleResult> resultFalsePositiveTotal = new LinkedList<>();
  private final List<RelationDesc> resultFalseNegativeTotal = new LinkedList<>();

  private final HashMap<String, List<PatternMatchSingleResult>> resultType = new HashMap<>();
  private final HashMap<String, List<PatternMatchSingleResult>> resultTruePositiveType = new HashMap<>();
  private final HashMap<String, List<PatternMatchSingleResult>> resultFalsePositiveType = new HashMap<>();
  private final HashMap<String, List<RelationDesc>> resultFalseNegativeType = new HashMap<>();


  public ActionMatchRelationsSet() {
    super("match-relations-set");
    setDescription("Reads node annotations for semantic relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getRuleFilenameOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getReportFileNameOption());
    options.addOption(CommonOptions.getVerifyRelationsOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    rulesFilename = line.getOptionValue(CommonOptions.OPTION_RULE_FILENAME);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);

    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
    if (line.hasOption(CommonOptions.OPTION_VERIFY_RELATIONS)) {
      verifyRelationsMode = true;
    }
    if (line.hasOption(CommonOptions.OPTION_VERBOSE)) {
      verbose = true;
    }
  }


  @Override
  public void run() throws Exception {

    preprocessParameters();


    patterns.forEach(pattern -> processOnePattern(pattern));

    if (verifyRelationsMode) {
      // preproceessFinalResults();
    }

    writeResultsToFile();
  }


  private void processOnePattern(final String pattern) {
    System.out.println("Processing pattern: " + pattern);
    //printHeapSize();

    final List<PatternMatchSingleResult> thisPatternResults = new LinkedList<>();
    final List<PatternMatchSingleResult> thisPatternResultsTruePositive = new LinkedList<>();
    final List<PatternMatchSingleResult> thisPatternResultsFalsePositive = new LinkedList<>();
    //final List<RelationDesc> thisPatternResultsFalseNegative = new LinkedList<>();

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

              matchDocTreeAgainstPatternTree(comboedDoc, patternMatch);

              documentResult.stream().forEach(r -> r.patternMatch = patternMatch);

              thisPatternResults.addAll(documentResult);
              thisPatternResultsTruePositive.addAll(documentResultTruePositive);
              thisPatternResultsFalsePositive.addAll(documentResultFalsePositive);
              //thisPatternResultsFalseNegative.addAll(documentResultFalseNegative);

              //documentName2documentResultTruePositive.put(comboedDoc.getName(), documentResultTruePositive);

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
    //patternsResultsFalseNegative.add(thisPatternResultsFalseNegative);


  }

  private void /* List<PatternMatchSingleResult>*/ matchDocTreeAgainstPatternTree(final Document d, final PatternMatch patternMatch) {

    //final List<PatternMatchSingleResult> documentResult = new ArrayList<>();
    documentResult = new ArrayList<>();
    documentResultTruePositive = new ArrayList<>();
    documentResultFalsePositive = new ArrayList<>();
    //documentResultFalseNegative = new ArrayList<>();

    int sentenceIndex = 0;
    for (final Sentence sentence : d.getParagraphs().get(0).getSentences()) {
      sentenceIndex++;
      try {

        final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceIndex);
        //final List<PatternMatchSingleResult> sentenceResults = patternMatch.getSentenceTreesMatchingSerelPattern(sentence);
        final List<PatternMatchSingleResult> sentenceResults = patternMatch.getSentenceTreesMatchingGenericPattern(sentence);

        for (final PatternMatchSingleResult patternMatchSingleResult : sentenceResults) {
          patternMatchSingleResult.sentenceNumber = sentenceIndex;
          patternMatchSingleResult.docName = d.getName();
        }

        documentResult.addAll(sentenceResults);

        if (verifyRelationsMode) {
          classifyResult(sentenceResults, smv, patternMatch);

//        if ((this.sentenceResultsOK.size() > 0) || (this.sentenceResultsNotHit.size() > 0) || (this.sentenceResultsFalseHit.size() > 0)) {
//          System.out.println("PROK=" + sentenceResultsOK);
//          System.out.println("PRFH=" + sentenceResultsFalseHit);
//          System.out.println("PRNH=" + sentenceResultsNotHit);
//        }

          documentResultTruePositive.addAll(sentenceResultsTruePositive);
          documentResultFalsePositive.addAll(sentenceResultsFalsePositive);
          //documentResultFalseNegative.addAll(sentenceResultsFalseNegative);
        }
      } catch (final Throwable th) {
        th.printStackTrace();
        System.out.println("Problem : " + th);
      }
    }
    //return documentResult;
  }

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
    //sentenceResultsFalseNegative = allSentenceNamRels;
  }

/*
  private final void preproceessFinalResults() {

    try (
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))
    ) {
      reader.forEach(comboedDoc -> {
            try {
              List<PatternMatchSingleResult> docTruePositives = documentName2documentResultTruePositive.get(comboedDoc.getName());

              int sentenceIndex = 0;
              for (final Sentence sentence : comboedDoc.getParagraphs().get(0).getSentences()) {

                // dla każdego takieog zdania sprawdź dla każdej relacji jeśli żaden wzorzec
                // jej nie wykrył to ląduje w FalseNegative'ach
                sentenceIndex++;
                try {

                  final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceIndex);

                  final List<RelationDesc> allSentenceNamRels = smv.getAllNamRels();

                  for (RelationDesc rd : allSentenceNamRels) {


                  }


                  resultFalseNegativeTotal.


                }
              }


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


  }

 */


  private void writeResultsToFile() throws Exception {
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    final BufferedWriter ow = new BufferedWriter(new OutputStreamWriter(os));

    final DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    df.setMinimumFractionDigits(2);


    for (int i = 0; i < patternsResults.size(); i++) {
      ow.write("\n");
      final String pattern = patterns.get(i);
      final List<PatternMatchSingleResult> result = patternsResults.get(i);

      final List<PatternMatchSingleResult> resultTruePositive = patternsResultsTruePositive.get(i);
      final List<PatternMatchSingleResult> resultFalsePositive = patternsResultsFalsePositive.get(i);
      //final List<RelationDesc> resultFalseNegative = patternsResultsFalseNegative.get(i);

      ow.write("Dla wzorca ='" + pattern + "'\n");
      ow.write("\t" + result.size() + "\t- znalezionych wyników \n");

      for (final PatternMatchSingleResult pmsr : result) {
        accumulateInTotalResult(pmsr);
        accumulateInTypeResult(pmsr);
      }


      if (verifyRelationsMode) {
        final double precision = (double) resultTruePositive.size() / (double) result.size();
        ow.write("\t" + resultTruePositive.size() + "\t- poprawne dopasowania\n");
        ow.write("\t" + resultFalsePositive.size() + "\t-  niepoprawne dopasowania\n");
        ow.write("\t" + df.format(precision) + "\t- precyzja\n");
        if (resultFalsePositive.size() > 0) {
          ow.write("\tNiepoprawne dopasowania to:\n");

          for (final PatternMatchSingleResult pmsr : resultFalsePositive) {
            ow.write("\t\t" + pmsr.descriptionLong() + "\n");
          }
        }

//        if (verbose) {
//          ow.write("pattern='" + pattern + "'" + "\t" + pmsr.descriptionLong() + "\n");
//        }

        for (final PatternMatchSingleResult pmsr : resultTruePositive) {
          accumulateInTotalResultTruePositive(pmsr);
          accumulateInTypeResultTruePositive(pmsr);
        }

        for (final PatternMatchSingleResult pmsr : resultFalsePositive) {
          accumulateInTotalResultFalsePositive(pmsr);
          accumulateInTypeResultFalsePositive(pmsr);
        }

//        for (final RelationDesc rd : resultFalseNegative) {
//          accumulateInTotalResultFalseNegative(rd);
//          accumulateInTypeResultFalseNegative(rd);
//        }
      }
    }


    ow.write("\n\n");
    ow.write("Sumarycznie zagregowane dla typów relacji: \n");

    resultType.keySet().stream().forEach(type -> {

      try {
        ow.write("\n\n");
        ow.write("Sumarycznie dla typu: " + type + "\n");
        ow.write("\t" + resultType.get(type).size() + "\t- znalezionych wyników ogółem\n");

        if (verifyRelationsMode) {
          resultType.computeIfAbsent(type, k -> new LinkedList<>());
//          resultFalseNegativeType.computeIfAbsent(type, k -> new LinkedList<>());
          resultFalsePositiveType.computeIfAbsent(type, k -> new LinkedList<>());
          resultTruePositiveType.computeIfAbsent(type, k -> new LinkedList<>());

//          for (final PatternMatchSingleResult pmsr : resultType.get(type)) {
//            for (int i = 0; i < resultFalseNegativeType.get(type).size(); i++) {
//              final RelationDesc relDesc = resultFalseNegativeType.get(type).get(i);
//              if (pmsr.isTheSameAs(relDesc)) {
//                resultFalseNegativeType.get(type).remove(i); // actually was hit
//              }
//            }
//          }

          ow.write("\t" + resultTruePositiveType.get(type).size() + "\t- poprawne dopasowania\n");
          ow.write("\t" + resultFalsePositiveType.get(type).size() + "\t- niepoprawne dopasowania\n");
          //ow.write("\t" + resultFalseNegativeType.get(type).size() + "\t- nieznalezione dopasowania\n");
          final double precision = (double) resultTruePositiveType.get(type).size() / (double) resultType.get(type).size();
          ow.write("\t" + df.format(precision) + "\t- precyzja\n");
          //final double recall = (double) resultTruePositiveType.get(type).size() / (double) (resultTruePositiveType.get(type).size() + resultFalseNegativeType.get(type).size());
          //ow.write("\t" + df.format(recall) + "\t- kompletność\n");

          if (resultTruePositiveType.get(type).size() > 0) {
            ow.write("\tPoprawne dopasowania to:\n");

            for (final PatternMatchSingleResult pmsr : resultTruePositiveType.get(type)) {
              ow.write("\t\t" + pmsr.descriptionLong() + "\n");
            }
          }


          if (resultFalsePositiveType.get(type).size() > 0) {
            ow.write("\tNiepoprawne dopasowania to:\n");

            for (final PatternMatchSingleResult pmsr : resultFalsePositiveType.get(type)) {
              ow.write("\t\t" + pmsr.descriptionLong() + "\n");
            }
          }


//          if (resultFalseNegativeType.get(type).size() > 0) {
//            ow.write("\tNieznalezione dopasowania to:\n");
//
//            for (final RelationDesc relDesc : resultFalseNegativeType.get(type)) {
//              ow.write("\t\t" + relDesc.toStringFull() + "\n");
//            }
//          }
        }

      } catch (final Exception e) {
        e.printStackTrace();
      }
    });


    ow.write("\n\n");
    ow.write("Sumarycznie dla wszystkich wzorców: \n");
    ow.write("\t" + resultTotal.size() + "\t- znalezionych wyników ogółem\n");

    if (verifyRelationsMode) {

      for (final PatternMatchSingleResult pmsr : resultTotal) {
        for (int i = 0; i < resultFalseNegativeTotal.size(); i++) {
          final RelationDesc relDesc = resultFalseNegativeTotal.get(i);
          if (pmsr.isTheSameAs(relDesc)) {
            resultFalseNegativeTotal.remove(i); // actually was hit
          }
        }
      }

      ow.write("\t" + resultTruePositiveTotal.size() + "\t- poprawne dopasowania\n");
      ow.write("\t" + resultFalsePositiveTotal.size() + "\t- niepoprawne dopasowania\n");
      ow.write("\t" + resultFalseNegativeTotal.size() + "\t- nieznalezione dopasowania\n");
      final double precision = (double) resultTruePositiveTotal.size() / (double) resultTotal.size();
      ow.write("\t" + df.format(precision) + "\t- precyzja\n");
      final double recall = (double) resultTruePositiveTotal.size() / (double) (resultTruePositiveTotal.size() + resultFalseNegativeTotal.size());
      ow.write("\t" + df.format(recall) + "\t- kompletność\n");

      if (resultFalsePositiveTotal.size() > 0) {
        ow.write("\tNiepoprawne dopasowania to:\n");

        for (final PatternMatchSingleResult pmsr : resultFalsePositiveTotal) {
          ow.write("\t\t" + pmsr.descriptionLong() + "\n");
        }
      }

      if (resultFalseNegativeTotal.size() > 0) {
        ow.write("\tNieznalezione dopasowania to:\n");
        for (final RelationDesc relDesc : resultFalseNegativeTotal) {
          ow.write("\t\t" + relDesc.toStringFull() + "\n");
        }
      }
    }

    ow.write("Saved to file:" + outputFilename);

    ow.flush();
    ow.close();

  }


  private void accumulateInTotalResult(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultTotal)) {
      resultTotal.add(pmsr);
    }
  }

  private void accumulateInTypeResult(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultType.computeIfAbsent(pmsr.getType(), k -> new LinkedList<>()))) {
      resultType.get(pmsr.getType()).add(pmsr);
    }
  }


  private void accumulateInTotalResultTruePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultTruePositiveTotal)) {
      resultTruePositiveTotal.add(pmsr);
    }
  }

  private void accumulateInTypeResultTruePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultTruePositiveType.computeIfAbsent(pmsr.getType(), k -> new LinkedList<>()))) {
      resultTruePositiveType.get(pmsr.getType()).add(pmsr);
    }
  }

  private void accumulateInTotalResultFalsePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultFalsePositiveTotal)) {
      resultFalsePositiveTotal.add(pmsr);
    }
  }

  private void accumulateInTypeResultFalsePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultFalsePositiveType.computeIfAbsent(pmsr.getType(), k -> new LinkedList<>()))) {
      resultFalsePositiveType.get(pmsr.getType()).add(pmsr);
    }
  }

  private boolean isAlreadyPresentInList(final PatternMatchSingleResult pmsr,
                                         final List<PatternMatchSingleResult> list) {
    for (final PatternMatchSingleResult p : list) {
      if (pmsr.isTheSameAs(p)) {
        return true;
      }
    }
    return false;
  }

  private void accumulateInTotalResultFalseNegative(final RelationDesc rd) {
    if (!isAlreadyPresentInList(rd, resultFalseNegativeTotal)) {
      resultFalseNegativeTotal.add(rd);
    }
  }

  private void accumulateInTypeResultFalseNegative(final RelationDesc rd) {
    if (!isAlreadyPresentInList(rd, resultFalseNegativeType.computeIfAbsent(rd.getType(), k -> new LinkedList<>()))) {
      resultFalseNegativeType.get(rd.getType()).add(rd);
    }
  }

  private boolean isAlreadyPresentInList(final RelationDesc relDesc, final List<RelationDesc> list) {
    for (final RelationDesc rd : list) {
      if (relDesc.isTheSameAs(rd)) {
        return true;
      }
    }
    return false;
  }


  private static void printHeapSize() {

    final long heapSize = Runtime.getRuntime().totalMemory();

    // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
    final long heapMaxSize = Runtime.getRuntime().maxMemory();

    // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
    final long heapFreeSize = Runtime.getRuntime().freeMemory();

    System.out.println("\t\t======Heapsize" + formatSize(heapSize));
    System.out.println("\t\theapssize\t\t" + formatSize(heapSize));
    System.out.println("\t\theapmaxsize\t\t" + formatSize(heapMaxSize));
    System.out.println("\t\theapFreesize\t\t" + formatSize(heapFreeSize));

  }

  private static String formatSize(final long v) {
    if (v < 1024) {
      return v + " B";
    }
    final int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }


  public void preprocessParameters() throws Exception {


    try {
      log.info("Wczytywanie Słowosieć 3.2 ...");
      WordnetPl32.load();
      //final WordnetPl wordnetPl = WordnetPl32.load();
    } catch (final Exception e) {
      e.printStackTrace();
    }

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
            patterns.add(pattern);
          } else {
            System.out.println("Commented out pattern: " + pattern);
          }
        }
      }
    }
    System.out.println("verify_relations mode  = " + verifyRelationsMode);
    System.out.println("Number of patterns found  = " + patterns.size());

  }


  private boolean isPatternARelation(final PatternMatch pattern) {
    if ((pattern.getRelationType() == null)
        ||
        (pattern.getRelationType().trim().isEmpty())) {
      return false;
    }

    return true;
  }


}

