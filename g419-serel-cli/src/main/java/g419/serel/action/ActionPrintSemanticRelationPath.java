package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.Pair;
import java.io.OutputStream;
import java.util.List;

public class ActionPrintSemanticRelationPath extends Action {

  public static final String OPTION_OUTPUT_ARG = "tree|tsv";

  private String inputFilename;
  private String inputFormat;
  private String output;
  private String outputFilename;

  private MaltParser malt;

  public ActionPrintSemanticRelationPath() {
    super("print-serel");
    setDescription("Reads relations from the documents and print them on the screen");
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

    malt = new MaltParser("/home/michalolek/NLPWR/projects/Liner2/190125_MALT_PDB");

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         ) {
      getLogger().info(" reading ...");

      reader.forEach(d -> printInfo(d));
    }
  }

  private void printInfo(Document document)  {
    try {
      getLogger().info(" Relacji jest :"+document.getRelationsSet().size());


      for (Relation rel : document.getRelations("Semantic relations")) {
        System.out.println(" Rel = " + rel);
        System.out.println(" Rel from= " + rel.getAnnotationFrom().getBaseText() + "  to = " + rel.getAnnotationTo().getBaseText());
        System.out.println(" Rel from head = " + rel.getAnnotationFrom().getHead() + "  to = " + rel.getAnnotationTo().getHead());

        int index1 = rel.getAnnotationFrom().getHead();
        int index2 = rel.getAnnotationTo().getHead();

        Sentence sentence = rel.getAnnotationFrom().getSentence();
        final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
        malt.parse(maltSentence);
        maltSentence.printAsTree();
        Pair<List<MaltSentenceLink>, List<MaltSentenceLink>> path = maltSentence.getPathBetween(index1, index2);
        System.out.println(path);
      }

    }
    catch(Exception e ) {
      e.printStackTrace();
    }

  }


}
