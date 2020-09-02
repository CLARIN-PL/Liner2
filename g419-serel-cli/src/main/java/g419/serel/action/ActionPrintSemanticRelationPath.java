package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import org.apache.commons.cli.CommandLine;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ActionPrintSemanticRelationPath extends Action {

  private String inputFilename;
  private String inputFormat;
  private String outputFilename;
  private String maltParserModelFilename;

  private MaltParser malt;

  public ActionPrintSemanticRelationPath() {
    super("print-serel");
    setDescription("Reads relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    maltParserModelFilename = line.getOptionValue(CommonOptions.OPTION_MALT);
  }

  @Override
  public void run() throws Exception {
    malt = new MaltParser(maltParserModelFilename);
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter pw =    new PrintWriter( new BufferedWriter(new OutputStreamWriter(os))) ) {
      reader.forEach(d ->  printInfo(d,pw));
    }
  }

  private void printInfo(Document document, PrintWriter pw)  {
    try {
      for (Relation rel : document.getRelations("Semantic relations")) {
        Sentence sentence = rel.getAnnotationFrom().getSentence();
        final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
        malt.parse(maltSentence);
        pw.println(maltSentence.getRelPathAsString(rel));
      }
    }
    catch(Exception e ) {
      e.printStackTrace();
    }
  }

}
