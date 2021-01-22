package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
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

  private final List<String> patterns = new LinkedList<>();
  private final List<List<PatternMatchSingleResult>> patternsResults = new LinkedList<>();

  @Getter
  //private List<PatternMatchSingleResult> result;
  private final List<PatternMatchSingleResult> resultTotal = new LinkedList<>();


  public ActionMatchRelationsSet() {
    super("match-relations-set");
    setDescription("Reads node annotations for semantic relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getRuleFilenameOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getReportFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    rulesFilename = line.getOptionValue(CommonOptions.OPTION_RULE_FILENAME);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);

    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
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
    System.out.println("Number of patterns found  = " + patterns.size());
    patterns.forEach(pattern -> processOnePattern(pattern));
    writeResultsToFile();
  }

  private void processOnePattern(final String pattern) {
    System.out.println("Processing pattern: " + pattern);
    final List<PatternMatchSingleResult> thisPatternResults = new LinkedList<>();


    final PatternMatch patternMatch = PatternMatch.parseRule(pattern);

    try (
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))
    ) {
      reader.forEach(comboedDoc -> {
            try {
              final List<PatternMatchSingleResult> result = matchDocTreeAgainstPatternTree(comboedDoc, patternMatch);
              thisPatternResults.addAll(result);
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

    System.out.println("Adding one entry with elements:" + thisPatternResults.size());
    patternsResults.add(thisPatternResults);

  }

  private List<PatternMatchSingleResult> matchDocTreeAgainstPatternTree(final Document d, final PatternMatch patternMatch) {
    final List<PatternMatchSingleResult> result = new ArrayList<>();

    int sentenceIndex = 0;
    for (final Sentence sentence : d.getParagraphs().get(0).getSentences()) {

      sentenceIndex++;
      // blokowało się na dokumencie 00100518 - zdanie 6 - ma węzeł który ma 10 podwęzłów bezpośrednich i wszystkich możliwośći
      // jest 10! = 3,628,800.
//      if (sentenceIndex != 6) {
//        continue;
//      }
      try {

        final SentenceMiscValues smv = SentenceMiscValues.from(sentence);
        //final List<PatternMatchSingleResult> docResults = patternMatch.getSentenceTreesMatchingSerelPattern(sentence);
        final List<PatternMatchSingleResult> docResults = patternMatch.getSentenceTreesMatchingGenericPattern(sentence);

        for (final PatternMatchSingleResult patternMatchSingleResult : docResults) {
          patternMatchSingleResult.sentenceNumber = sentenceIndex;
          patternMatchSingleResult.docName = d.getName();
        }

        result.addAll(docResults);
      } catch (final Throwable th) {
        th.printStackTrace();
        System.out.println("Problem : " + th);
      }

    }

    return result;
  }

  private void writeResultsToFile() throws Exception {
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    final BufferedWriter ow = new BufferedWriter(new OutputStreamWriter(os));

    for (int i = 0; i < patternsResults.size(); i++) {
      final String pattern = patterns.get(i);
      final List<PatternMatchSingleResult> result = patternsResults.get(i);
      ow.write("Dla wzorca ='" + pattern + "'" + "\tznaleziono " + result.size() + " wyników\n");

      for (final PatternMatchSingleResult pmsr : result) {

        ow.write("pattern='" + pattern + "'" + "\t" + pmsr.descriptionLong() + "\n");

        if (isAlreadyPresentInTotalResult(pmsr)) {
          continue;
        }
        resultTotal.add(pmsr);
      }
    }
    ow.write("Ogółem znaleziono " + resultTotal.size() + " wyników\n");

    ow.flush();
    ow.close();

  }

  private boolean isAlreadyPresentInTotalResult(final PatternMatchSingleResult pmsr) {
    for (final PatternMatchSingleResult p : resultTotal) {
      if (pmsr.isTheSameAs(p)) {
        return true;
      }
    }
    return false;
  }

}

