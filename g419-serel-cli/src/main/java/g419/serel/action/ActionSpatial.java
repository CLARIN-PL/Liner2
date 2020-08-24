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
import g419.spatial.tools.ISpatialRelationRecognizer;
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

  public ActionSpatial() {
    super("spatial");
    setDescription("recognize spatial relations");
    options.addOption(getOptionInputFilename());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption());
    options.addOption(CommonOptions.getWordnetOption(true));
  }

  private Option getOptionInputFilename() {
    return Option.builder(ActionSpatial.OPTION_FILENAME).hasArg().argName("FILENAME").required()
        .desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(ActionSpatial.OPTION_FILENAME);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    maltparserModel = line.getOptionValue(CommonOptions.OPTION_MALT);
    wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
  }

  @Override
  public void run() throws Exception {
    final Wordnet3 wordnet = new Wordnet3(wordnetPath);
    final MaltParser malt = new MaltParser(maltparserModel);
    final ISpatialRelationRecognizer recognizer = new SpatialRelationRecognizer(wordnet).withMaltParser(malt);
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat);

    Document document = null;
    while ((document = reader.nextDocument()) != null) {
      System.out.println("=======================================");
      System.out.println("Document: " + document.getName());
      System.out.println("=======================================");

      for (final Paragraph paragraph : document.getParagraphs()) {
        for (final Sentence sentence : paragraph.getSentences()) {

          final List<SpatialExpression> relations = recognizer.recognize(sentence);

          for (final SpatialExpression rel : relations) {
            System.out.println(rel.toString());
          }
        }
      }
    }

    reader.close();
  }

}
