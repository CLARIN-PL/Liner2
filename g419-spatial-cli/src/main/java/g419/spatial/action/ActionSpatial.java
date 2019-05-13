package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.SpatialRelationRecognizer;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.List;

public class ActionSpatial extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private String filename = null;
  private String inputFormat = null;

  private String maltparserModel = null;
  private String wordnetPath = null;

  /**
   *
   */
  public ActionSpatial() {
    super("spatial");
    this.setDescription("recognize spatial relations");
    this.options.addOption(this.getOptionInputFilename());
    this.options.addOption(CommonOptions.getInputFileFormatOption());
    this.options.addOption(CommonOptions.getMaltparserModelFileOption());
    this.options.addOption(CommonOptions.getWordnetOption(true));
  }

  /**
   * Create Option object for input file name.
   *
   * @return Object for input file name parameter.
   */
  private Option getOptionInputFilename() {
    return Option.builder(ActionSpatial.OPTION_FILENAME).hasArg().argName("FILENAME").required()
        .desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();
  }

  /**
   * Parse action options
   *
   * @param arg0 The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.filename = line.getOptionValue(ActionSpatial.OPTION_FILENAME);
    this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    this.maltparserModel = line.getOptionValue(CommonOptions.OPTION_MALT);
    this.wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
  }

  @Override
  public void run() throws Exception {
    Wordnet3 wordnet = new Wordnet3(this.wordnetPath);
    MaltParser malt = new MaltParser(this.maltparserModel);
    SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer(malt, wordnet);
    AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);

    Document document = null;
    while ((document = reader.nextDocument()) != null) {
      System.out.println("=======================================");
      System.out.println("Document: " + document.getName());
      System.out.println("=======================================");

      for (Paragraph paragraph : document.getParagraphs()) {
        for (Sentence sentence : paragraph.getSentences()) {

          List<SpatialExpression> relations = recognizer.recognize(sentence);

          for (SpatialExpression rel : relations) {
            System.out.println(rel.toString());
          }
        }
      }
    }

    reader.close();
  }

}
