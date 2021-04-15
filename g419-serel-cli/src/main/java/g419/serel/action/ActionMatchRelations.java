package g419.serel.action;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ActionMatchRelations /* extends Action */ {

  /*
  @Setter
  private String inputFilename;
  @Setter
  private String inputFormat;

  @Setter
  private String rule;
  private String ruleFilename;

  private String reportFilename;

  @Getter
  private List<PatternMatchSingleResult> result;



  public ActionMatchRelations() {
    super("match-relations");
    setDescription("Reads node annotations for semantic relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getRuleOption());
    options.addOption(CommonOptions.getRuleFilenameOption());

    options.addOption(CommonOptions.getReportFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    rule = line.getOptionValue(CommonOptions.OPTION_RULE);
    ruleFilename = line.getOptionValue(CommonOptions.OPTION_RULE_FILENAME);

    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {

    String searchingRule = rule;
    if ((ruleFilename != null) && (!ruleFilename.isEmpty())) {
      System.out.println(" Pattern filename = " + ruleFilename);
      final File file = new File(ruleFilename);
      final BufferedReader br = new BufferedReader(new FileReader(file));
      searchingRule = br.readLine();
    }

    System.out.println("Searching rule = " + searchingRule);
    final PatternMatch patternMatch = PatternMatch.parseRule(searchingRule);


    try (
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))
    ) {
      reader.forEach(comboedDoc -> {
            try {
              result = matchDocTreeAgainstPatternTree(comboedDoc, patternMatch);
              result.forEach(r -> System.out.println(r.description()));
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

  private List<PatternMatchSingleResult> matchDocTreeAgainstPatternTree(final Document d, final PatternMatch patternMatch) {
    final List<PatternMatchSingleResult> result = new ArrayList<>();

    int sentenceIndex = 0;
    for (final Sentence sentence : d.getParagraphs().get(0).getSentences()) {
      sentenceIndex++;
      try {
//        log.debug("");
//        log.debug(" Sentence nr " + sentenceIndex);
//        log.debug(" Sentence = " + sentence);

        final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceIndex);
        //final List<PatternMatchSingleResult> results = patternMatch.getSentenceTreesMatchingSerelPattern(sentence);
        final List<PatternMatchSingleResult> results = patternMatch.getSentenceTreesMatchingGenericPattern(sentence);

        for (final PatternMatchSingleResult patternMatchSingleResult : results) {
          patternMatchSingleResult.sentenceNumber = sentenceIndex;
          patternMatchSingleResult.docName = d.getName();
        }

        result.addAll(results);
      } catch (final Throwable th) {
        th.printStackTrace();
        System.out.println("Problem : " + th);
      }

    }

    return result;
  }

   */

}
