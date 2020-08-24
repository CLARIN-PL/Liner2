package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.io.SpatialOutputFormat;
import g419.spatial.io.writer.SpatialWriterFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.OutputStream;

public class ActionPrint extends Action {

  public static final String OPTION_OUTPUT_ARG = "tree|tsv";

  private String inputFilename;
  private String inputFormat;
  private String output;
  private String outputFilename;

  public ActionPrint() {
    super("print");
    setDescription("Reads spatial expressions from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(Option.builder(CommonOptions.OPTION_OUTPUT_FORMAT)
        .longOpt(CommonOptions.OPTION_OUTPUT_FORMAT_LONG).hasArg().argName(OPTION_OUTPUT_ARG).required().build());
    options.addOption(CommonOptions.getOutputFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    output = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
  }

  @Override
  public void run() throws Exception {
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final AbstractDocumentWriter writer = SpatialWriterFactory.create(SpatialOutputFormat.valueOf(output.toUpperCase()), os)) {
      reader.forEach(writer::writeDocument);
    }
  }

}
