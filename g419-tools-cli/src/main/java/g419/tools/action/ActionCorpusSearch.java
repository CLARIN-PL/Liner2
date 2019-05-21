package g419.tools.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

public class ActionCorpusSearch extends Action {

  private final String OPTION_QUERY_LONG = "query";
  private final String OPTION_QUERY = "q";

  private String inputFilename = null;
  private String inputFormat = null;
  private String query = null;

  public ActionCorpusSearch() {
    super("corpus-search");
    this.setDescription("wyszukuje zdania zawierające określone frazy");
    this.options.addOption(CommonOptions.getInputFileFormatOption());
    this.options.addOption(CommonOptions.getInputFileNameOption());
    this.options.addOption(Option.builder(OPTION_QUERY).longOpt(OPTION_QUERY_LONG).hasArg().argName("phrase")
        .desc("fraza do znalezienia").required().required().build());
  }

  /**
   * Parse action options
   *
   * @param line The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE_LONG);
    this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT_LONG);
    this.query = line.getOptionValue(OPTION_QUERY_LONG);
  }

  @Override
  public void run() throws Exception {

    Document document = null;
    AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);

    String query = this.query.toLowerCase();

    System.out.println("Search query: " + query);

    while ((document = reader.nextDocument()) != null) {
      Logger.getLogger(this.getClass()).info("Document: " + document.getName());
      for (Sentence sentence : document.getSentences()) {
        for (int i = 0; i < sentence.getTokens().size(); i++) {
          if (this.matches(sentence, i, query) > 0) {
            System.out.println(this.sentenceWithHighlightToString(sentence, query));
            break;
          }
        }
      }
    }

  }

  /**
   * @param sentence
   * @param query
   * @return
   */
  private String sentenceWithHighlightToString(Sentence sentence, String query) {
    StringBuilder sb = new StringBuilder();
    int matched = 0;
    boolean bracketOpen = false;
    for (int i = 0; i < sentence.getTokenNumber(); i++) {
      Token token = sentence.getTokens().get(i);
      if (matched == 0) {
        matched = this.matches(sentence, i, query);
        if (matched > 0) {
          sb.append("[");
          bracketOpen = true;
          matched--;
        }
      }
      sb.append(token.getOrth());
      if (matched == 0 && bracketOpen) {
        sb.append("]");
        bracketOpen = false;
      }
      if (token.getNoSpaceAfter() == false) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  /**
   * @param sentence
   * @param position
   * @param query
   * @return
   */
  private int matches(Sentence sentence, int position, String query) {
    Token token = sentence.getTokens().get(position);
    if (token.getOrth().toLowerCase().startsWith(query)) {
      return 1;
    } else {
      return 0;
    }
  }
}
