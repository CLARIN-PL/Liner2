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
import g419.serel.structure.ParseTreeMalfunction;
import g419.serel.structure.SerelExpression;
import org.apache.commons.cli.CommandLine;
import javax.swing.text.html.parser.Parser;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
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
    setDescription("Checks validity of Malt parse tree and print results on the screen");
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

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter writer = new PrintWriter( os) )
    {
      List<ParseTreeMalfunction> result = new LinkedList<>();
      reader.forEach(doc->result.addAll(checkMaltParseTree(doc)));
      writer.println("Code\tdocument\tann_id\tsourceIndex\ttargetIndex\tstartAnnIndex\tendAnnIndex");
      result.stream().forEach(line -> writer.println(line.getMalfunctionCode()+"\t"+
                                                     line.getDocumentPath()+"\t"+
                                                     line.getAnnotationId()+"\t"+
                                                     line.getSourceIndex()+"\t"+
                                                     line.getTargetIndex()+"\t"+
                                                     line.getAnnStartRange()+"\t"+
                                                     line.getAnnEndRange()
      ));
    }
  }

  private List<ParseTreeMalfunction> checkMaltParseTree(Document document ) {
    List<ParseTreeMalfunction> result = new LinkedList<>();

    final List<SerelExpression> serelExpressions = converter.convert(document);
    for(SerelExpression se : serelExpressions ) {

      Annotation aFrom = se.getRelation().getAnnotationFrom();
      result.addAll(isAnnotationHeadPointingOut(se, aFrom));
      result.addAll(isHavingMoreElementsPointingOut(se, aFrom));

      Annotation aTo = se.getRelation().getAnnotationTo();
      result.addAll(isAnnotationHeadPointingOut(se, aTo));
      result.addAll(isHavingMoreElementsPointingOut(se, aFrom));
    }
    return result;
  }

  /*
  Widać, że nie zawsze głowa jest elementem "wychodzącym" z nazwy, np.
  "location: nam_org_organization -> wielki -> finał -> z -> problem <- w <- Wrocław <- nam_loc_gpe_city" :
  bierzemy node i sprawdzamy czy link z "głowy" wychodzi ma zewnątrz
  */
  private List<ParseTreeMalfunction> isAnnotationHeadPointingOut(SerelExpression se, Annotation a) {

    List<ParseTreeMalfunction> result = new LinkedList<>();

    int headForOut = a.getHead();
    Optional<MaltSentenceLink> linkForOut = se.getMaltSentence().getLinksBySourceIndex(headForOut);
    if(linkForOut.isPresent()) {
      int targetOutIndex = linkForOut.get().getTargetIndex();
      if (a.isTokenIndexWithin(targetOutIndex)) {

        ParseTreeMalfunction ptm = ParseTreeMalfunction.builder()
            .malfunctionCode(ParseTreeMalfunction.MalfunctionCode.AHPI)
            .documentPath(se.getSentence().getDocument().getName())
            .annotationId(a.getId())
            .sourceIndex(headForOut)
            .targetIndex(targetOutIndex)
            .annStartRange(a.getBegin())
            .annEndRange((a.getEnd()))
            .build();
        result.add(ptm);
      }
    }
    return result;
  }


  /*
   2. sprawdzenie czy są przypadki, gdzie w relacji była nazwa wielowyrazowa
   i oba elementy nazwy wskazywały na element poza nazwą :
   bierzemy node, jeśli nazwa jest wielowyrazowa to sprawdzamy czy nie jest czasem
   tak, że więcej niż jeden wyraz wskazuje na zewnątrz "poza" nazwą
  */
  private List<ParseTreeMalfunction> isHavingMoreElementsPointingOut(SerelExpression se, Annotation ann) {
    List<ParseTreeMalfunction> result = new LinkedList<>();

    List<MaltSentenceLink> links = ann.getTokens()
        .stream()
        .map(index -> se.getMaltSentence().getLinksBySourceIndex(index))
        .filter(optLink -> optLink.isPresent())
        .filter(optLink -> ! ann.isTokenIndexWithin(optLink.get().getTargetIndex()))
        .map(optLink -> optLink.get())
        .collect(Collectors.toList());

    if (links.size() > 1) {
      for (MaltSentenceLink maltSentenceLink : links) {
        ParseTreeMalfunction ptm = ParseTreeMalfunction.builder()
            .malfunctionCode(ParseTreeMalfunction.MalfunctionCode.MEPO)
            .documentPath(se.getSentence().getDocument().getName())
            .annotationId(ann.getId())
            .sourceIndex(maltSentenceLink.getSourceIndex())
            .targetIndex(maltSentenceLink.getTargetIndex())
            .annStartRange(ann.getBegin())
            .annEndRange((ann.getEnd()))
            .build();

        result.add(ptm);
      }
    }
    return result;
  }



}
