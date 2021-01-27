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
import java.util.ArrayList;
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

  private final List<List<PatternMatchSingleResult>> patternsResultsOK = new LinkedList<>();
  private List<PatternMatchSingleResult> sentenceResultsOK;

  private final List<List<PatternMatchSingleResult>> patternsResultsFalseHit = new LinkedList<>();
  private List<PatternMatchSingleResult> sentenceResultsFalseHit;

  private final List<List<RelationDesc>> patternsResultsNotHit = new LinkedList<>();
  private List<RelationDesc> sentenceResultsNotHit;

  List<PatternMatchSingleResult> resultOK = new ArrayList<>();
  List<PatternMatchSingleResult> resultFalseHit = new ArrayList<>();
  List<RelationDesc> resultNotHit = new ArrayList<>();

  List<PatternMatchSingleResult> documentResultOK = new ArrayList<>();
  List<PatternMatchSingleResult> documentResultFalseHit = new ArrayList<>();
  List<RelationDesc> documentResultNotHit = new ArrayList<>();

  @Getter
  //private List<PatternMatchSingleResult> result;
  private final List<PatternMatchSingleResult> resultTotal = new LinkedList<>();
  private final List<PatternMatchSingleResult> resultOKTotal = new LinkedList<>();
  private final List<PatternMatchSingleResult> resultFalseHitTotal = new LinkedList<>();
  private final List<RelationDesc> resultNotHitTotal = new LinkedList<>();


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

    if ((rulesFilename != null) && (!rulesFilename.isEmpty())) {
      System.out.println(" Patterns filename = " + rulesFilename);
      final File file = new File(rulesFilename);
      final BufferedReader br = new BufferedReader(new FileReader(file));

      String pattern;
      while ((pattern = br.readLine()) != null) {
        if (!pattern.isEmpty()) {
          patterns.add(pattern);
        }
      }
    }
    System.out.println("verify_relations mode  = " + verifyRelationsMode);
    System.out.println("Number of patterns found  = " + patterns.size());
    patterns.forEach(pattern -> processOnePattern(pattern));
    writeResultsToFile();
  }

  private boolean isPatternARelation(final PatternMatch pattern) {
    if ((pattern.getRelationType() == null)
        ||
        (pattern.getRelationType().trim().isEmpty())) {
      return false;
    }

    return true;
  }

  private void processOnePattern(final String pattern) {
    System.out.println("Processing pattern: " + pattern);

    final List<PatternMatchSingleResult> thisPatternResults = new LinkedList<>();
    final List<PatternMatchSingleResult> thisPatternResultsOK = new LinkedList<>();
    final List<PatternMatchSingleResult> thisPatternResultsFalseHit = new LinkedList<>();
    final List<RelationDesc> thisPatternResultsNotHit = new LinkedList<>();

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
              final List<PatternMatchSingleResult> documentResult = matchDocTreeAgainstPatternTree(comboedDoc, patternMatch);
              thisPatternResults.addAll(documentResult);

              thisPatternResultsOK.addAll(documentResultOK);
              thisPatternResultsFalseHit.addAll(documentResultFalseHit);
              thisPatternResultsNotHit.addAll(documentResultNotHit);

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

    //System.out.println("Adding one entry with elements:" + thisPatternResults.size());
    patternsResults.add(thisPatternResults);
    patternsResultsOK.add(thisPatternResultsOK);
    patternsResultsFalseHit.add(thisPatternResultsFalseHit);
    patternsResultsNotHit.add(thisPatternResultsNotHit);

  }

  private List<PatternMatchSingleResult> matchDocTreeAgainstPatternTree(final Document d, final PatternMatch patternMatch) {

    final List<PatternMatchSingleResult> documentResult = new ArrayList<>();

    documentResultOK = new ArrayList<>();
    documentResultFalseHit = new ArrayList<>();
    documentResultNotHit = new ArrayList<>();

    int sentenceIndex = 0;
    for (final Sentence sentence : d.getParagraphs().get(0).getSentences()) {

      sentenceIndex++;

      try {

        final SentenceMiscValues smv = SentenceMiscValues.from(sentence);
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


          documentResultOK.addAll(sentenceResultsOK);
          documentResultFalseHit.addAll(sentenceResultsFalseHit);
          documentResultNotHit.addAll(sentenceResultsNotHit);
        }

      } catch (final Throwable th) {
        th.printStackTrace();
        System.out.println("Problem : " + th);
      }

    }

    return documentResult;
  }

  private void classifyResult(final List<PatternMatchSingleResult> sentenceResults, final SentenceMiscValues smv, final PatternMatch patternMatch) {

    sentenceResultsOK = new LinkedList<>();
    sentenceResultsFalseHit = new LinkedList<>();

    //final List<RelationDesc> allSentenceNamRels = smv.getAllNamRels();
    final List<RelationDesc> allSentenceNamRels = smv.getNamRelsMatchingRelation(patternMatch);

    outer:
    for (final PatternMatchSingleResult pmsr : sentenceResults) {

      for (int i = 0; i < allSentenceNamRels.size(); i++) {
        final RelationDesc rd = allSentenceNamRels.get(i);
        if (pmsr.isTheSameAs(rd)) {
          allSentenceNamRels.remove(i);
          sentenceResultsOK.add(pmsr);
          continue outer;
        }
      }
      sentenceResultsFalseHit.add(pmsr);
    }
    sentenceResultsNotHit = allSentenceNamRels;
  }


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

      final List<PatternMatchSingleResult> resultOK = patternsResultsOK.get(i);
      final List<PatternMatchSingleResult> resultFalseHit = patternsResultsFalseHit.get(i);
      final List<RelationDesc> resultNotHit = patternsResultsNotHit.get(i);


      ow.write("Dla wzorca ='" + pattern + "'\n");
      ow.write("\t" + result.size() + "\t- znalezionych wyników \n");

      for (final PatternMatchSingleResult pmsr : result) {
        accumulateInTotalResult(pmsr);
      }


      if (verifyRelationsMode) {
        final double precision = (double) resultOK.size() / (double) result.size();
        ow.write("\t" + resultOK.size() + "\t- poprawne dopasowania\n");
        ow.write("\t" + resultFalseHit.size() + "\t-  niepoprawne dopasowania\n");
        ow.write("\t" + df.format(precision) + "\t- precyzja\n");
        if (resultFalseHit.size() > 0) {
          ow.write("\tNiepoprawne dopasowania to:\n");

          for (final PatternMatchSingleResult pmsr : resultFalseHit) {
            ow.write("\t\t" + pmsr.descriptionLong() + "\n");
          }
        }

//        if (verbose) {
//          ow.write("pattern='" + pattern + "'" + "\t" + pmsr.descriptionLong() + "\n");
//        }

        for (final PatternMatchSingleResult pmsr : resultOK) {
          accumulateInTotalResultOK(pmsr);
        }

        for (final PatternMatchSingleResult pmsr : resultFalseHit) {
          accumulateInTotalResultFalseHit(pmsr);
        }
      }

    }


    ow.write("\n\n");
    ow.write("Sumarycznie dla wszystkich wzorców: \n");
    ow.write("\t" + resultTotal.size() + "\t- znalezionych wyników ogółem\n");

    if (verifyRelationsMode) {


      for (int i = 0; i < resultNotHit.size(); i++) {
        final RelationDesc relDesc = resultNotHit.get(i);
        for (final PatternMatchSingleResult pmsr : resultTotal) {
          if (pmsr.isTheSameAs(relDesc)) {
            resultNotHit.remove(i); // actually was hit
          }
        }
      }


      ow.write("\t" + resultOKTotal.size() + "\t- poprawne dopasowania\n");
      ow.write("\t" + resultFalseHitTotal.size() + "\t- niepoprawne dopasowania\n");
      ow.write("\t" + resultNotHitTotal.size() + "\t- nieznalezione dopasowania\n");
      final double precision = (double) resultOKTotal.size() / (double) resultTotal.size();
      ow.write("\t" + df.format(precision) + "\t- precyzja\n");
      final double recall = (double) resultOKTotal.size() / (double) (resultOKTotal.size() + resultNotHitTotal.size());
      ow.write("\t" + df.format(recall) + "\t- kompletność\n");

      if (resultFalseHitTotal.size() > 0) {
        ow.write("\tNiepoprawne dopasowania to:\n");

        for (final PatternMatchSingleResult pmsr : resultFalseHitTotal) {
          ow.write("\t\t" + pmsr.descriptionLong() + "\n");
        }
      }

      if (resultFalseHitTotal.size() > 0) {
        ow.write("\tNieznalezione dopasowania to:\n");

        for (final RelationDesc relDesc : resultNotHitTotal) {
          ow.write("\t\t" + relDesc.toString() + "\n");
        }
      }


    }

    ow.flush();
    ow.close();

  }


  private void accumulateInTotalResult(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInTotalResult(pmsr)) {
      resultTotal.add(pmsr);
    }
  }

  private boolean isAlreadyPresentInTotalResult(final PatternMatchSingleResult pmsr) {
    for (final PatternMatchSingleResult p : resultTotal) {
      if (pmsr.isTheSameAs(p)) {
        return true;
      }
    }
    return false;
  }

  private void accumulateInTotalResultOK(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInTotalResultOK(pmsr)) {
      resultOKTotal.add(pmsr);
    }
  }

  private boolean isAlreadyPresentInTotalResultOK(final PatternMatchSingleResult pmsr) {
    for (final PatternMatchSingleResult p : resultOKTotal) {
      if (pmsr.isTheSameAs(p)) {
        return true;
      }
    }
    return false;
  }

  private void accumulateInTotalResultFalseHit(final PatternMatchSingleResult pmsr) {
    if (!isAlreadyPresentInTotalResultFalseHit(pmsr)) {
      resultFalseHitTotal.add(pmsr);
    }
  }

  private boolean isAlreadyPresentInTotalResultFalseHit(final PatternMatchSingleResult pmsr) {
    for (final PatternMatchSingleResult p : resultFalseHitTotal) {
      if (pmsr.isTheSameAs(p)) {
        return true;
      }
    }
    return false;
  }

}

