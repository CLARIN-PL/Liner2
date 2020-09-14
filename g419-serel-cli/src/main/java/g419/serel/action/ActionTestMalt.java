package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.SentenceLink;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;
import java.util.List;

public class ActionTestMalt extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private String filename = null;
  private String inputFormat = null;

  public ActionTestMalt() {
    super("test-malt");
    setDescription("recognize semantic relations");
    options.addOption(getOptionInputFilename());
    options.addOption(CommonOptions.getInputFileFormatOption());
  }

  /**
   * Create Option object for input file name.
   *
   * @return Object for input file name parameter.
   */
  private Option getOptionInputFilename() {
    return Option.builder(OPTION_FILENAME).longOpt(OPTION_FILENAME_LONG)
        .hasArg().argName("filename").required().desc("path to the input file").build();
  }

  /**
   * Parse action options
   *
   * @param line The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(ActionTestMalt.OPTION_FILENAME);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
  }

  @Override
  public void run() throws Exception {
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat);
    Document document = reader.nextDocument();

    final MaltParser malt = new MaltParser("/home/michalolek/NLPWR/projects/Liner2/190125_MALT_PDB");

    while (document != null) {
      Logger.getLogger(getClass()).info("\nDocument: " + document.getName());

      for (final Paragraph paragraph : document.getParagraphs()) {
        for (final Sentence sentence : paragraph.getSentences()) {
          final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
          malt.parse(maltSentence);
          maltSentence.printAsTree();

          List<SentenceLink> result  =  maltSentence.getParentsAscending(sentence.getTokens().size()-1);
          System.out.println("Result size = "+result.size() );
          result.stream().forEach(System.out::println);

          for (int i = 0; i < sentence.getTokens().size(); i++) {
            final Token token = sentence.getTokens().get(i);
            System.out.println(token);
          }

          for (int i = 0; i < maltSentence.getLinks().size(); i++) {
            final SentenceLink msl = maltSentence.getLinks().get(i);
            System.out.println(msl);
          }

          /*
          for (int i = 0; i < sentence.getChunks().size(); i++) {
             final Set<Annotation> chunk = sentence.getChunks();
             System.out.println( "---");
             for(Annotation ann : chunk )
               System.out.println(ann);
          }
          */

//          final List<SpatialExpression> srs = findByMalt(sentence, maltSentence);
//          for (final SpatialExpression sr : srs) {
//            System.out.println(sr.toString());
//          }
        }
      }
      document = reader.nextDocument();
    }

    reader.close();
  }


}
