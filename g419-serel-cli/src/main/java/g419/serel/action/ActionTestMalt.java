package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.structure.SpatialExpression;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ActionTestMalt extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private String filename = null;
  private String inputFormat = null;

  public ActionTestMalt() {
    super("test-malt");
    setDescription("recognize spatial relations");
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
   * @param arg0 The array with command line parameters
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

    final MaltParser malt = new MaltParser("/nlp/resources/maltparser/skladnica_liblinear_stackeager_final.mco");

    while (document != null) {
      Logger.getLogger(getClass()).info("\nDocument: " + document.getName());

      for (final Paragraph paragraph : document.getParagraphs()) {
        for (final Sentence sentence : paragraph.getSentences()) {
          final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
          malt.parse(maltSentence);

          final List<SpatialExpression> srs = findByMalt(sentence, maltSentence);
          for (final SpatialExpression sr : srs) {
            System.out.println(sr.toString());
          }
        }
      }
      document = reader.nextDocument();
    }

    reader.close();
  }

  /**
   * @param sentence
   * @param maltSentence
   * @return
   */
  public List<SpatialExpression> findByMalt(final Sentence sentence, final MaltSentence maltSentence) {
    final List<SpatialExpression> srs = new ArrayList<>();
    for (int i = 0; i < sentence.getTokens().size(); i++) {
      final Token token = sentence.getTokens().get(i);
      final List<Integer> landmarks = new ArrayList<>();
      final List<Integer> trajectors = new ArrayList<>();
      Integer indicator = null;
      final String type = "MALT";
      String typeLM = "";
      String typeTR = "";
      if (ClassFeature.BROAD_CLASSES.get("verb").contains(token.getDisambTag().getPos())) {
        final List<MaltSentenceLink> links = maltSentence.getLinksByTargetIndex(i);
        for (final MaltSentenceLink link : links) {
          final Token tokenChild = sentence.getTokens().get(link.getSourceIndex());
          if (link.getRelationType().equals("subj")) {
            if (tokenChild.getDisambTag().getBase().equals("i")
                || tokenChild.getDisambTag().getBase().equals("oraz")) {
              typeTR = "_TRconj";
              for (final MaltSentenceLink trLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())) {
                landmarks.add(trLink.getSourceIndex());
              }
            } else if (!tokenChild.getDisambTag().getPos().equals("interp")) {
              trajectors.add(link.getSourceIndex());
            }
          } else if (tokenChild.getDisambTag().getPos().equals("prep")) {
            indicator = link.getSourceIndex();
            for (final MaltSentenceLink prepLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())) {
              final Token lm = sentence.getTokens().get(prepLink.getSourceIndex());
              if (lm.getOrth().equals(",")) {
                typeLM = "_LMconj";
                for (final MaltSentenceLink prepLinkComma : maltSentence.getLinksByTargetIndex(prepLink.getSourceIndex())) {
                  landmarks.add(prepLinkComma.getSourceIndex());
                }
              } else if (!lm.getDisambTag().getPos().equals("interp")) {
                landmarks.add(prepLink.getSourceIndex());
              }
            }
          }
        }
      }

      if (landmarks.size() > 0 && trajectors.size() > 0 && indicator != null) {
        for (final Integer landmark : landmarks) {
          for (final Integer trajector : trajectors) {
            final SpatialExpression sr = new SpatialExpression(
                type + typeLM + typeTR,
                new Annotation(trajector, "trajector", sentence),
                new Annotation(indicator, "indicator", sentence),
                new Annotation(landmark, "landmark", sentence));
            srs.add(sr);
          }
        }
      }
    }
    return srs;
  }

}
