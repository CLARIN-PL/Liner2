package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;

import java.io.File;

/**
 * Chunking in pipe mode.
 *
 * @author Maciej Janicki, Michał Marcińczuk
 */
public class ActionInplace extends Action {

  private String input_file = null;
  private String input_format = null;

  public ActionInplace() {
    super("inplace");
    setDescription("process documents in place");

    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getFeaturesOption());
    options.addOption(CommonOptions.getModelFileOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
  }

  /**
   * Module entry function.
   */
  @Override
  public void run() throws Exception {

    if (!LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER)) {
      throw new ParameterException("Parameter 'chunker' in 'main' section of model not set");
    }

    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(input_file, input_format);
    TokenFeatureGenerator gen = null;

    if (!LinerOptions.getGlobal().features.isEmpty()) {
      gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
    }

    /* Create all defined chunkers. */
    final ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
    cm.loadChunkers();

    final Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

    final String outputTmpIndex = input_file + ".tmp";
    final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(outputTmpIndex, input_format);

    Document document = null;
    while ((document = reader.nextDocument()) != null) {
      System.out.println(document.getUri());
      if (gen != null) {
        gen.generateFeatures(document);
      }
      chunker.chunkInPlace(document);
      writer.writeDocument(document);
    }

    reader.close();
    writer.close();

    final File tmp = new File(outputTmpIndex);
    if (tmp.exists()) {
      tmp.delete();
    }
  }

}
