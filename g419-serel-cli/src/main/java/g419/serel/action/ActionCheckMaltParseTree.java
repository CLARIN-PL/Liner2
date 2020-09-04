package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.structure.SerelExpression;
import org.apache.commons.cli.CommandLine;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActionCheckMaltParseTree extends Action {

  private String inputFilename;
  private String inputFormat;
  private String outputFilename;
  private String outputFormat;
  private String maltParserModelFilename;
  private String reportFilename;

  private MaltParser malt;
  DocumentToSerelExpressionConverter converter;

  public ActionCheckMaltParseTree() {
    super("check-malt-tree");
    setDescription("Reads relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(CommonOptions.getMaltparserModelFileOption());
    options.addOption(CommonOptions.getReportFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    outputFormat = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
    maltParserModelFilename = line.getOptionValue(CommonOptions.OPTION_MALT);
    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {
    malt = new MaltParser(maltParserModelFilename);
    final OutputStream os = WriterFactory.get().getOutputStreamFileOrOut(outputFilename);
    converter = new DocumentToSerelExpressionConverter(malt,null);

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat))
         //final AbstractDocumentWriter writer = SerelWriterFactory.create(SerelOutputFormat.valueOf(outputFormat.toUpperCase()), os, malt,pw))

    {



      reader.forEach(doc->checkMaltParseTree(doc));
    }
  }

  private void checkMaltParseTree(Document document ) {

    final List<SerelExpression> serelExpressions = converter.convert(document);
    for(SerelExpression se : serelExpressions ) {

      Annotation aFrom = se.getRelation().getAnnotationFrom();
      isAnnotationHeadPointingOut(se, aFrom);
      if (isHavingMoreElementsPointingOut(se,aFrom)) {
        System.out.println(" DOC: "+ se.getSentence().getDocument().getName()+   " ann = "+aFrom.getId());
      }

      Annotation aTo = se.getRelation().getAnnotationTo();
      isAnnotationHeadPointingOut(se, aTo);
      if (isHavingMoreElementsPointingOut(se,aFrom)) {
        System.out.println(" DOC: "+ se.getSentence().getDocument().getName()+   " ann = "+aTo.getId());
      }



    }


    //Widać, że nie zawsze głowa jest elementem "wychodzącym" z nazwy, np.
    //"location: nam_org_organization -> wielki -> finał -> z -> problem <- w <- Wrocław <- nam_loc_gpe_city"
    //    |
    //    |
    //    V
    // bierzemy node i sprawdzamy czy link z "głowy" wychodzi ma zewnątrz

    // 2. sprawdzenie czy są przypadki, gdzie w relacji była nazwa wielowyrazowa
    // i oba elementy nazwy wskazywały na element poza nazwą
    //    |
    //    |
    //    V
    // bierzemy node, jeśli nazwa jest wielowyrazowa to sprawdzamy czy nie jest czasem
    // tak, że więcej niż jeden wyraz wskazuje na zewnątrz "poza" nazwą

  }


  private boolean isAnnotationHeadPointingOut(SerelExpression se, Annotation a) {
    int headForOut = a.getHead();
    Optional<MaltSentenceLink> linkForOut = se.getMaltSentence().getLinksBySourceIndex(headForOut);
    if(linkForOut.isPresent()) {
      int targetOutIndex = linkForOut.get().getTargetIndex();
      if (a.isTokenIndexWithin(targetOutIndex)) {
        System.out.println(" DOC: "+ se.getSentence().getDocument().getName()+   " headIndex = "+headForOut+ " headToken=" +a.getHeadToken().getOrth()   + " targetOutIndex =" + targetOutIndex+ " ["+a.getBegin()+":"+a.getEnd()+"] "+a.getText());
        return false;
      }
    }
    return true;
  }

  private boolean isHavingMoreElementsPointingOut(SerelExpression se,Annotation ann) {
    List<MaltSentenceLink> result = ann.getTokens()
        .stream()
        .map(index -> se.getMaltSentence().getLinksBySourceIndex(index))
        .filter(optLink -> optLink.isPresent())
        .filter(optLink -> ann.isTokenIndexWithin(optLink.get().getTargetIndex()))
        .map(optLink -> optLink.get())
        .collect(Collectors.toList());

    if (result.size() > 1) {
      System.out.println("Ann =" + ann);
      for (MaltSentenceLink maltSentenceLink : result) {
        System.out.println("\tlink = " + maltSentenceLink);
      }
      return true;
    }
    return false;
  }



}
