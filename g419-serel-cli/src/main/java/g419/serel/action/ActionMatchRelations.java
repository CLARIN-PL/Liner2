package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.liner2.core.tools.parser.SentenceLink;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.structure.RuleMatchingRelations;
import g419.serel.structure.SentenceMiscValues;
import g419.serel.structure.SerelExpression;
import g419.serel.tools.ComboParseTreeGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.tuple.Pair;
import sun.awt.X11.XSelectionRequestEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class ActionMatchRelations extends Action {

  private String inputFilename;
  private String inputFormat;

  private String rule;
  private String ruleFilename;

  private String reportFilename;

  public ActionMatchRelations() {
    super("match-relations");
    setDescription("Reads node annotations for semantic relations from the documents and print them on the screen");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getRuleOption());
    options.addOption(CommonOptions.getRuleFilenameOption());

    options.addOption(CommonOptions.getReportFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    rule = line.getOptionValue(CommonOptions.OPTION_RULE);
    ruleFilename = line.getOptionValue(CommonOptions.OPTION_RULE_FILENAME);

    reportFilename = line.getOptionValue(CommonOptions.OPTION_REPORT_FILE);
  }

  @Override
  public void run() throws Exception {


      System.out.println("Searching rule = "+rule);
      String searchingRule= rule;

//      // z opisu
//    //String searchingRule = "location:: w (case)> [nam_loc_gpe_country] (nmod) :target > * < [nam_loc_gpe_city] (nmod) :source";
//
//      // 1 bez deprel
//    //String searchingRule =" location:: [nam_fac_goe] :source > * > [nam_loc_gpe_city] :target ";
//
//        // 2 z deprel dobrym
//      String searchingRule =" location:: [nam_fac_goe] (root) :source > * > [nam_loc_gpe_city] (nmod) :target ";
//
//      // 3 z deprel złym
//     // String searchingRule =" location:: [nam_fac_goe] (root) :source > * > [nam_loc_gpe_city] (case) :target ";


    RuleMatchingRelations rmr = RuleMatchingRelations.understandRule(searchingRule);
    System.out.println(searchingRule);
    System.out.println("Understood rule ="+rmr);


    //System.out.println("Starting reading ...");
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter reportWriter = reportFilename==null? null :new PrintWriter( new FileWriter( new File(reportFilename)))
         ) {


        reader.forEach(doc-> {
                    try {
                        //Document comboedDoc = reader.nextDocument();
                        Document comboedDoc = doc;
                        ParseTreeGenerator parseTreeGenerator = new ComboParseTreeGenerator(comboedDoc);
                        DocumentToSerelExpressionConverter converter = new DocumentToSerelExpressionConverter(parseTreeGenerator,reportWriter);

                        //List<SerelExpression> list = converter.convert(doc);

                        List<RelationDesc> result = matchRelationsAgainstRule(doc,  rmr, converter);

                        //result.forEach(System.out::println);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

    } catch (Exception e) {
      e.printStackTrace();
    }


  }



    private List<RelationDesc> matchRelationsAgainstRule(Document d, RuleMatchingRelations rmr, DocumentToSerelExpressionConverter converter) {

        List<RelationDesc> result = new ArrayList<>();

        int sentenceIndex =1;
        for (Sentence sentence : d.getParagraphs().get(0).getSentences()) {
            System.out.println("");
            System.out.println(" Sentence nr "+sentenceIndex);
            System.out.println(" Sentence = " + sentence);
            sentenceIndex++;

            SentenceMiscValues smv  = SentenceMiscValues.from(sentence);

            Set<RelationDesc> rels1 = smv.getTokenIndexesForMatchingRelType(rmr,null);
            System.out.println("rels1="+rels1);
            if(rels1.isEmpty())
                continue;

            Set<RelationDesc> rels2 = smv.getTokenIndexesForMatchingRelNE(rmr,rels1);
            System.out.println("rels2="+rels2);
            if(rels2.isEmpty())
                continue;

            // tutaj wiemy że typ i kotwice tych relacji są na pewno dobre

            List<SerelExpression> serels = converter.convertAlreadyComboedFromRelDesc(rels2);

            for (SerelExpression serel : serels) {
                //System.out.println("serel="+serel.getPathAsString());
                System.out.println("serel="+serel.getDetailedPathAsString(true));
            }


            Set<SerelExpression> rels3 = smv.getRelationsMatchingRule(rmr,serels);
            System.out.println("rels3="+rels3);
            if(rels3.isEmpty())
                continue;

        }
        return result;
    }








}
