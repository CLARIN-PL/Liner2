package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.serel.io.SerelOutputFormat;
import g419.serel.io.writer.SerelWriterFactory;
import org.apache.commons.cli.CommandLine;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ActionPrintSemanticRelationPath extends Action {

  private String inputFilename;
  private String inputFormat;
  private String outputFilename;
  private String outputFormat;
  private String maltParserModelFilename;
  private String reportFilename;

  private MaltParser malt;

  public ActionPrintSemanticRelationPath() {
    super("print-serel");
    setDescription("Reads relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption());
    options.addOption(CommonOptions.getReportFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    outputFormat = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
    maltParserModelFilename = line.getOptionValue(CommonOptions.OPTION_MALT);
    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {
    malt = new MaltParser(maltParserModelFilename);
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter pw = (reportFilename!=null)?new PrintWriter( new File(reportFilename) ): null;
         final AbstractDocumentWriter writer = SerelWriterFactory.create(SerelOutputFormat.valueOf(outputFormat.toUpperCase()), os, malt,pw)) {
      reader.forEach(writer::writeDocument);
    }
  }


}
