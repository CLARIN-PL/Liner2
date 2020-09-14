package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.serel.tools.CheckParserParseTree;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.structure.ParseTreeMalfunction;
import g419.serel.tools.ComboParseTreeGenerator;
import g419.serel.tools.MaltParseTreeGenerator;
import org.apache.commons.cli.CommandLine;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class ActionCheckComboParseTree extends Action {

  private String inputFilename;
  private String inputFormat;
  private String outputFilename;
  private String outputFormat;
  private String comboFilename;
  private String reportFilename;


  DocumentToSerelExpressionConverter converter;
  private CheckParserParseTree  parseTreeChecker;
  private ParseTreeGenerator  parseTreeGenerator;

  public ActionCheckComboParseTree() {
    super("check-combo-tree");
    setDescription("Checks validity of Combo parse tree and print results on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getComboFileNameOption());

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

    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {

    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    parseTreeGenerator = new ComboParseTreeGenerator(comboFilename);
    converter = new DocumentToSerelExpressionConverter(parseTreeGenerator,null);

    parseTreeChecker = new CheckParserParseTree();

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter writer = new PrintWriter( os) )
    {
      List<ParseTreeMalfunction> result = new LinkedList<>();

      reader.forEach(doc->result.addAll(parseTreeChecker.checkParseTree(converter.convert(doc))));

      System.out.println("REsult size = "+result.size());

      writer.println("Code\tdocument\tann_id\tsourceIndex\ttargetIndex\tstartAnnIndex\tendAnnIndex");
      result.stream().forEach(line -> writer.println(line.getMalfunctionCode()+"\t"+
                                                     line.getDocumentPath()+"\t"+
                                                     line.getAnnotationId()+"\t"+
                                                     line.getSourceIndex()+"\t"+
                                                     line.getTargetIndex()+"\t"+
                                                     line.getAnnStartRange()+"\t"+
                                                     line.getAnnEndRange()
      ));
    }
  }

}
