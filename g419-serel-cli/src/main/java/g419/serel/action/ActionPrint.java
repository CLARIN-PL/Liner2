package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;

public class ActionPrint extends Action {

  private String inputFilename;
  private String inputFormat;

  public ActionPrint() {
    super("print");
    setDescription("Reads node annotations for semantic relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
  }

  @Override
  public void run() throws Exception {
    System.out.println("Starting reading ...");
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         ) {
      System.out.println("Isnide try");
      reader.forEach(d -> printInfo(d));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void printInfo(Document d) {
    System.out.println(" Sentence si9ze  = "+d.getParagraphs().get(0).getSentences().get(0).getTokens());
    getLogger().error(" Relations set size :"+d.getRelationsSet().size());
    for( Relation rel : d.getRelationsSet()) {
         getLogger().error(" Rel from= "+rel.getAnnotationFrom().getBaseText() +"  to = "+rel.getAnnotationTo().getBaseText());
    }
  }

}
