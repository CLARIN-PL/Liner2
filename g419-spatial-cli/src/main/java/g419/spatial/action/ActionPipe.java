package g419.spatial.action;

import com.google.common.base.Optional;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.tools.SpatialRelationRecognizer;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import reactor.core.publisher.Flux;

public class ActionPipe extends Action {

  private Optional<String> inputFilename;
  private Optional<String> inputFormat;
  private Optional<String> outputFilename;
  private Optional<String> outputFormat;
  private Optional<String> maltparserModel;
  private Optional<String> wordnetPath;

  /**
   *
   */
  public ActionPipe() {
    super("pipe");
    setDescription("recognize spatial expressions and add them to the document as a set of frames");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption());
    options.addOption(CommonOptions.getWordnetOption(true));
  }

  /**
   * Parse action options
   *
   * @param line The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_INPUT_FILE));
    inputFormat = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT));
    outputFilename = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE));
    outputFormat = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT));
    maltparserModel = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_MALT));
    wordnetPath = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_WORDNET));
  }

  @Override
  public void run() throws Exception {
    final Wordnet3 wordnet = new Wordnet3(wordnetPath.get());
    final MaltParser malt = new MaltParser(maltparserModel.get());
    final SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer(malt, wordnet);

    try (
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename.get(), inputFormat.get());
        AbstractDocumentWriter writer = getWriter(outputFilename, outputFormat)
    ) {
      Flux.fromIterable(reader)
          .doOnNext(doc -> getLogger().info("Processing document {}", doc.getName()))
          .doOnNext(recognizer::recognizeInPlace)
          .doOnNext(writer::writeDocument)
          .subscribe();
    }
  }

  private AbstractDocumentWriter getWriter(final Optional<String> outputFilename, final Optional<String> outputFormat) throws Exception {
    return outputFilename.isPresent()
        ? WriterFactory.get().getStreamWriter(outputFilename.get(), outputFormat.get())
        : WriterFactory.get().getStreamWriter(System.out, outputFormat.get());
  }
}
