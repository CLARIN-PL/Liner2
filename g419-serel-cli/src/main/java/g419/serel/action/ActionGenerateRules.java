package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.structure.SerelExpression;
import g419.serel.tools.CheckParserParseTree;
import org.apache.commons.cli.CommandLine;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class ActionGenerateRules extends Action {

  private String inputFilename;
  private String inputFormat;
  private String outputFilename;
  private String outputFormat;
  private String comboFilename;
  private String comboFormat;
  private String reportFilename;

  private boolean uposMode;
  private boolean xposMode;
  private boolean deprelMode;
  private boolean caseMode;

  private boolean extMode = false;
  private boolean treeMode = false;


  DocumentToSerelExpressionConverter converter;
  private CheckParserParseTree parseTreeChecker;
  private ParseTreeGenerator parseTreeGenerator;

  public ActionGenerateRules() {
    super("generate-rules");
    setDescription("Generates rules from conllu files");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getComboFileNameOption());
    options.addOption(CommonOptions.getComboFileFormatOption());


    options.addOption(CommonOptions.getUPosModeOption());
    options.addOption(CommonOptions.getXPosModeOption());
    options.addOption(CommonOptions.getDeprelModeOption());
    options.addOption(CommonOptions.getCaseModeOption());

    options.addOption(CommonOptions.getExtModeOption());
    options.addOption(CommonOptions.getTreeModeOption());

    options.addOption(CommonOptions.getReportFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    outputFormat = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
    if ((outputFormat == null) || (outputFormat.isEmpty())) {
      outputFormat = "TSV";
    }
    comboFilename = line.getOptionValue(CommonOptions.OPTION_COMBO_FILE);
    comboFormat = line.getOptionValue(CommonOptions.OPTION_COMBO_FORMAT);

    if (line.hasOption(CommonOptions.OPTION_UPOS_MODE)) {
      uposMode = true;
    }
    if (line.hasOption(CommonOptions.OPTION_XPOS_MODE)) {
      xposMode = true;
    }
    if (line.hasOption(CommonOptions.OPTION_DEPREL_MODE)) {
      deprelMode = true;
    }
    if (line.hasOption(CommonOptions.OPTION_CASE_MODE)) {
      caseMode = true;
    }


    if (line.hasOption(CommonOptions.OPTION_EXT_PATTERN_MODE)) {
      extMode = true;
    }
    if (line.hasOption(CommonOptions.OPTION_TREE_MODE)) {
      treeMode = true;
    }

    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {

    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);


    parseTreeChecker = new CheckParserParseTree();

    try (
        final AbstractDocumentReader comboReader = ReaderFactory.get().getStreamReader(comboFilename, comboFormat);
        final PrintWriter writer = new PrintWriter(os);
        final PrintWriter reportWriter = reportFilename == null ? null : new PrintWriter(new FileWriter(new File(reportFilename)))) {


      final DocumentToSerelExpressionConverter converter = new DocumentToSerelExpressionConverter(null, reportWriter);

      comboReader.forEach(comboedDoc -> {
            try {

              System.out.println("processing comboedDoc =" + comboedDoc.getName());

              final List<SerelExpression> converted = converter.convert(comboedDoc);

              for (final SerelExpression serel : converted) {
                this.reportSerel(serel, reportWriter);
              }
            } catch (final Exception e) {
              e.printStackTrace();
            }
          }
      );

      converter.typesCounter.keySet().stream().forEach(k -> System.out.println(k + " : " + converter.typesCounter.get(k)));
      int total = 0;

      for (final String key : converter.typesCounter.keySet()) {
        total += converter.typesCounter.get(key);
      }
      System.out.println("TOTAL: " + total);

    } catch (final Exception e) {
      throw e;
    }
  }

  private void reportSerel(final SerelExpression se, final PrintWriter reportWriter) {

    if (extMode) {
      // WHOLE REPORT
      reportWriter.println(se.getPathAsString(uposMode, xposMode, deprelMode, caseMode));
    } else {
      //JUST PATTERNS
      reportWriter.println(se.getJustPattern(uposMode, xposMode, deprelMode, caseMode));
    }

    if (treeMode) {
      // GENERATE TREE
      se.getSentence().printAsTree(reportWriter);
      reportWriter.println("------------------------------------------------------");
    }
  }


}
