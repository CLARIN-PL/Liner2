package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;

public class ActionStats extends Action {

  private String input_file = null;
  private String input_format = null;

  public ActionStats() {
    super("stats");
    setDescription("prints corpus statistics");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
  }

  @Override
  public void run() throws Exception {
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(input_file, input_format);
    int documents = 0;
    int sentences = 0;
    int tokens = 0;
    int annotations = 0;
    while (reader.hasNext()) {
      final Document document = reader.next();
      documents++;
      for (final Sentence sentence : document.getSentences()) {
        sentences++;
        tokens += sentence.getTokenNumber();
        annotations += sentence.getChunks().size();
      }
    }

    final String line = "%20s: %10d";
    System.out.println(String.format(line, "Documents", documents));
    System.out.println(String.format(line, "Sentences", sentences));
    System.out.println(String.format(line, "Tokens", tokens));
    System.out.println(String.format(line, "Annotations", annotations));
  }

}
