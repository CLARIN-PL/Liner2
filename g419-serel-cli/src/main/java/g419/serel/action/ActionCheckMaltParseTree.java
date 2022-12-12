package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.serel.tools.CheckParserParseTree;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.structure.ParseTreeMalfunction;
import g419.serel.tools.MaltParseTreeGenerator;
import org.apache.commons.cli.CommandLine;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class ActionCheckMaltParseTree extends Action {

  private String inputFilename;
  private String inputFormat;
  private String outputFilename;
  private String outputFormat;
  private String maltParserModelFilename;
  private String reportFilename;

  private MaltParser malt;
  DocumentToSerelExpressionConverter converter;
  private CheckParserParseTree  parseTreeChecker;
  private ParseTreeGenerator parseTreeGenerator;

  public ActionCheckMaltParseTree() {
    super("check-malt-tree");
    setDescription("Checks validity of Malt parse tree and print results on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption(true));
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
    maltParserModelFilename = line.getOptionValue(CommonOptions.OPTION_MALT);
    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {

    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter writer = new PrintWriter( os) ;
         final PrintWriter reportWriter = reportFilename==null? null : new PrintWriter( new FileWriter( new File(reportFilename))) )
    {
      malt = new MaltParser(maltParserModelFilename);
      parseTreeGenerator = new MaltParseTreeGenerator(malt);
      converter = new DocumentToSerelExpressionConverter(parseTreeGenerator,reportWriter);
      parseTreeChecker = new CheckParserParseTree();

      List<ParseTreeMalfunction> result = new LinkedList<>();
      reader.forEach(doc->result.addAll(parseTreeChecker.checkParseTree(converter.convert(doc))));
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
