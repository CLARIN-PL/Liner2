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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


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
  boolean verbose;

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
  private final List<List<RelationDesc>> patternsResultsFalseNegative = new LinkedList<>();

  List<PatternMatchSingleResult> documentResult = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultTruePositive = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultFalsePositive = new ArrayList<>();
  Set<RelationDesc> documentResultFalseNegative = new HashSet<>();

  Map<String, Set<PatternMatchSingleResult>> documentName2documentResultTruePositive = new HashMap<>();
  Map<String, Set<RelationDesc>> documentName2documentResultFalseNegative = new HashMap<>();
  Map<String, Map<Integer, List<RelationDesc>>> resultFalseNegativeTotalMap = new HashMap<>();

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

  final DecimalFormat df = new DecimalFormat();

  {
    df.setMaximumFractionDigits(2);
    df.setMinimumFractionDigits(2);
  }

  public ActionMatchRelationsSet() {
    super("match-relations-set");
    setDescription("Reads node annotations for semantic relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getRuleFilenameOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getReportFileNameOption());
    options.addOption(CommonOptions.getPrintSectionsOption());
    options.addOption(CommonOptions.getVerifyRelationsOption());
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


    preproceessFinalResults();


//    documentName2documentResultTruePositive.keySet().stream()
//        .forEach(k -> System.out.println("key=" + k + " size =" + documentName2documentResultTruePositive.get(k).size()));


    writeResultsToFile();
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
                documentName2documentResultTruePositive.put(comboedDoc.getName(), new HashSet<>());
              }


              matchDocTreeAgainstPatternTree(comboedDoc, patternMatch);

              documentResult.stream().forEach(r -> r.patternMatch = patternMatch);

              thisPatternResults.addAll(documentResult);
              thisPatternResultsTruePositive.addAll(documentResultTruePositive);
              thisPatternResultsFalsePositive.addAll(documentResultFalsePositive);

              documentName2documentResultTruePositive.get(comboedDoc.getName()).addAll(documentResultTruePositive);

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
        final List<PatternMatchSingleResult> sentenceResults = patternMatch.getSentenceTreesMatchingGenericPattern(sentence);

        /*
        for (final PatternMatchSingleResult patternMatchSingleResult : sentenceResults) {
          patternMatchSingleResult.sentenceNumber = sentenceNumber;
          patternMatchSingleResult.docName = d.getName();
          // patternMatchSingleResult.patternMatch = patternMatch;
        }
        */

        documentResult.addAll(sentenceResults);

        classifyResult(sentenceResults, smv, patternMatch);

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


  private final void preproceessFinalResults() throws Exception {

    try (
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))
    ) {


      reader.forEach(comboedDoc -> {


        try {
          final Set<PatternMatchSingleResult> docTruePositives = documentName2documentResultTruePositive.get(comboedDoc.getName());

//          if (comboedDoc.getName().equals("documents/00102158")) {
//            System.out.println(" TP = " + docTruePositives.size());
//          }


          int sentenceIndex = 0;
          for (final Sentence sentence : comboedDoc.getParagraphs().get(0).getSentences()) {
            // dla każdego takieog zdania sprawdź dla każdej relacji jeśli żaden wzorzec
            // jej nie wykrył to relacja ląduje w FalseNegative'ach
            sentenceIndex++;
            try {
              final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceIndex);
              final List<RelationDesc> allSentenceNamRels = smv.getAllNamRels();
              for (final RelationDesc rd : allSentenceNamRels) {

//                if (comboedDoc.getName().equals("documents/00102158")) {
//                  System.out.println("checking RD = " + rd);
//                }


                final Optional<PatternMatchSingleResult> matching = docTruePositives.stream().filter(pmsr -> pmsr.isTheSameAs(rd)).findAny();

                if (!matching.isPresent()) {
//                  System.out.println("NO MATCH !!!");
//                  System.out.println(" WARN! Doc " + comboedDoc.getName() + " Marking as false negative :" + rd);
                  resultType.computeIfAbsent(rd.getType(), k -> new LinkedList<>());
                  documentResultFalseNegative.add(rd);
                } else {
//                  System.out.println("MATCH !!!" + matching.get().description());
                }
              }
            } catch (final Exception e) {
              System.out.println("Problem z dokumentem " + comboedDoc.getName());
              e.printStackTrace();
            }
          }

          //System.out.println("Ilość false negatiwów w dokumencie:" + comboedDoc.getName() + " wynosi :" + documentResultFalseNegative.size());
          documentName2documentResultFalseNegative.put(comboedDoc.getName(), documentResultFalseNegative);
          documentResultFalseNegative = new HashSet<>();

        } catch (final Exception e) {
          System.out.println("2 Problem z dokumentem " + comboedDoc.getName());
          e.printStackTrace();
        }


      });


    } catch (final Exception e) {
      System.out.println("Jakis problemm przy przeliczaniu");
      e.printStackTrace();
    }

  }


  private void writeResultsToFile() throws Exception {
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    final BufferedWriter ow = new BufferedWriter(new OutputStreamWriter(os));

    final DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    df.setMinimumFractionDigits(2);


    // wyniki wyświetlane dla każdego pojedynczego wzorca
    for (int i = 0; i < patternsResults.size(); i++) {
      //ow.write("\n");
      final String pattern = patterns.get(i);
      final List<PatternMatchSingleResult> result = patternsResults.get(i);
      final List<PatternMatchSingleResult> resultTruePositive = patternsResultsTruePositive.get(i);
      final List<PatternMatchSingleResult> resultFalsePositive = patternsResultsFalsePositive.get(i);

      for (final PatternMatchSingleResult pmsr : result) {
        accumulateInTotalResult(pmsr);
        accumulateInTypeResult(pmsr);
      }


      printResultLine(ow,
          result.size(),
          resultTruePositive.size(),
          resultFalsePositive.size(),
          null,  // dla pojedynczego wzor5ca nie liczymy false negatiwów
          pattern
      );

      printSections(ow,
          false,
          printSectionTruePositive,
          printSectionFalsePositive,
          false,
          result,
          resultTruePositive,
          resultFalsePositive,
          null
      );


      for (final PatternMatchSingleResult pmsr : resultTruePositive) {
        accumulateInTotalResultTruePositive(pmsr);
        accumulateInTypeResultTruePositive(pmsr);
      }

      for (final PatternMatchSingleResult pmsr : resultFalsePositive) {
        accumulateInTotalResultFalsePositive(pmsr);
        accumulateInTypeResultFalsePositive(pmsr);
      }

    }

    resultType.keySet().stream().forEach(type -> {
      resultFalseNegativeType.computeIfAbsent(type, k -> new LinkedList<>());
    });

    documentName2documentResultFalseNegative.values().stream().forEach(docRFN -> {
      resultFalseNegativeTotal.addAll(docRFN);
    });

    documentName2documentResultFalseNegative
        .values()
        .stream()
        .forEach(docRFN -> docRFN
            .stream()
            .forEach(rd -> resultFalseNegativeType.get(rd.getType()).add(rd))
        );

//    //System.out.println("Checking ....");
//    resultType.keySet().stream().forEach(type -> {
//      System.out.println(" size rFNT " + type + " = " + resultFalseNegativeType.get(type).size());
//    });

    //FINISH:  preprocessing for typed and total data


    ow.write("\n\n\n");
    //ow.write("Sumarycznie zagregowane dla " + resultType.keySet().size() + " typów relacji: \n");

    resultType.keySet().stream().forEach(type -> {

      try {
        resultType.computeIfAbsent(type, k -> new LinkedList<>());
        resultFalsePositiveType.computeIfAbsent(type, k -> new LinkedList<>());
        resultTruePositiveType.computeIfAbsent(type, k -> new LinkedList<>());
        resultFalseNegativeType.computeIfAbsent(type, k -> new LinkedList<>());


        printResultLine(ow,
            resultType.get(type).size(),
            resultTruePositiveType.get(type).size(),
            resultFalsePositiveType.get(type).size(),
            resultFalseNegativeType.get(type).size(),
            "Sumarycznie dla typu " + type
        );

        printSections(ow,
            false,
            printSectionTruePositive,
            printSectionFalsePositive,
            printSectionFalseNegative,
            resultType.get(type),
            resultTruePositiveType.get(type),
            resultFalsePositiveType.get(type),
            resultFalseNegativeType.get(type)
        );

      } catch (final Exception e) {
        e.printStackTrace();
      }
    });


    ow.write("\n\n");

    printResultLine(ow,
        resultTotal.size(),
        resultTruePositiveTotal.size(),
        resultFalsePositiveTotal.size(),
        resultFalseNegativeTotal.size(),
        "TOTAL"
    );

    printSections(ow,
        printSectionFound,
        printSectionTruePositive,
        printSectionFalsePositive,
        printSectionFalseNegative,
        resultTotal,
        resultTruePositiveTotal,
        resultFalsePositiveTotal,
        resultFalseNegativeTotal
    );

    System.out.println("Saved to file:" + outputFilename);

    ow.flush();
    ow.close();

  }


  private void printResultLine(final BufferedWriter ow,
                               final Integer found,
                               final Integer truePositive,
                               final Integer falsePositive,
                               final Integer falseNegative,
                               final String description)
      throws IOException {


    ow.write("\t" + truePositive + ";\t");
    ow.write("\t" + falsePositive + ";\t");
    final double precision = (double) truePositive / (double) (truePositive + falsePositive);
    ow.write("\t" + df.format(precision) + ";");

    double recall = 0;
    if (falseNegative != null) {
      ow.write("\t" + falseNegative + ";\t");
      recall = (double) truePositive / (double) (truePositive + falseNegative);
      ow.write("\t" + df.format(recall) + ";\t");
    } else {
      ow.write("\t;");
      ow.write("\t;");
    }

    if (falseNegative != null) {
      final double f1 = 2 * (precision * recall) / (precision + recall);
      ow.write("\t" + df.format(f1) + ";\t ");
    } else {
      ow.write("\t;");
    }


    ow.write(description + ";");
    if ((truePositive != null) && (falseNegative != null)) {
      ow.write("total for type: " + (truePositive + falseNegative) + ";");
    }

    ow.write("\n");


  }

  private void printSections(
      final BufferedWriter ow,
      final boolean _printSectionFound,
      final boolean _printSectionTruePositive,
      final boolean _printSectionFalsePositive,
      final boolean _printSectionFalseNegative,
      final List<PatternMatchSingleResult> _resultFound,
      final List<PatternMatchSingleResult> _resultTruePositive,
      final List<PatternMatchSingleResult> _resultFalsePositive,
      final List<RelationDesc> _resultFalseNegative
  )
      throws IOException {

    if (_printSectionFound) {
      if (_resultFound.size() > 0) {
        ow.write("\tZnalezione dopasowania to:\n");

        Collections.sort(_resultFound, new PatternMatchSingleResult.SortByDocument());
        for (final PatternMatchSingleResult pmsr : _resultFound) {
          ow.write("\t\t" + pmsr.descriptionLong() + "\n");
        }
      }
    }

    if (_printSectionTruePositive) {
      if (_resultTruePositive.size() > 0) {
        ow.write("\tPoprawne dopasowania to:\n");

        Collections.sort(_resultTruePositive, new PatternMatchSingleResult.SortByDocument());
        for (final PatternMatchSingleResult pmsr : _resultTruePositive) {
          ow.write("\t\t" + pmsr.descriptionLong() + "\n");
        }
      }
    }

    if (_printSectionFalsePositive) {
      if (_resultFalsePositive.size() > 0) {
        ow.write("\tNiepoprawne dopasowania to:\n");

        Collections.sort(_resultFalsePositive, new PatternMatchSingleResult.SortByDocument());
        for (final PatternMatchSingleResult pmsr : _resultFalsePositive) {
          ow.write("\t\t" + pmsr.descriptionLong() + "\n");
        }
      }
    }

    if (_printSectionFalseNegative) {
      if (_resultFalseNegative.size() > 0) {
        ow.write("\tNieznalezione dopasowania to:\n");
        Collections.sort(_resultFalseNegative, new RelationDesc.SortByDocument());
        for (final RelationDesc relDesc : _resultFalseNegative) {
          ow.write("\t\t" + relDesc.toStringFull() + "\n");
        }
      }
    }
  }


  private void accumulateInTotalResult(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultTotal)) {
      resultTotal.add(pmsr);
    }
  }

  private void accumulateInTypeResult(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultType.computeIfAbsent(pmsr.getRelationType(), k -> new LinkedList<>()))) {
      resultType.get(pmsr.getRelationType()).add(pmsr);
    }
  }


  private void accumulateInTotalResultTruePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultTruePositiveTotal)) {
      resultTruePositiveTotal.add(pmsr);
    }
  }

  private void accumulateInTypeResultTruePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultTruePositiveType.computeIfAbsent(pmsr.getRelationType(), k -> new LinkedList<>()))) {
      resultTruePositiveType.get(pmsr.getRelationType()).add(pmsr);
    }
  }

  private void accumulateInTotalResultFalsePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultFalsePositiveTotal)) {
      resultFalsePositiveTotal.add(pmsr);
    }
  }

  private void accumulateInTypeResultFalsePositive(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInList(pmsr, resultFalsePositiveType.computeIfAbsent(pmsr.getRelationType(), k -> new LinkedList<>()))) {
      resultFalsePositiveType.get(pmsr.getRelationType()).add(pmsr);
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


}

