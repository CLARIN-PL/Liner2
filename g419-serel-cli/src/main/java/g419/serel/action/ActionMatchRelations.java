package g419.serel.action;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.serel.structure.ParsedRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;

@Slf4j
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
        log.debug("Searching rule = " + rule);
        final String searchingRule = rule;

        final ParsedRule parsedRule = ParsedRule.parseRule(searchingRule);
        log.debug(searchingRule);
        log.debug("Rule tree:");
        parsedRule.getRootNodeMatch().dumpString();


/*
    //log.debug("Starting reading ...");
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         final PrintWriter reportWriter = reportFilename==null? null :new PrintWriter( new FileWriter( new File(reportFilename)))
         ) {


        reader.forEach(doc-> {
                    try {
                        //Document comboedDoc = reader.nextDocument();
                        final Document comboedDoc = doc;
                        //System.out.println("START Doc = "+doc.getName());
                        final ParseTreeGenerator parseTreeGenerator = new ComboParseTreeGenerator(comboedDoc);
                        final DocumentToSerelExpressionConverter converter = new DocumentToSerelExpressionConverter(parseTreeGenerator,reportWriter);

                        //List<SerelExpression> list = converter.convert(doc);

                        final List<RelationDesc> result = matchRelationsAgainstRule(doc,  rmr, converter);

                        //result.forEach(System.out::println);

                    } catch (final Exception e) {
                        System.out.println("Problem z dokuementem "+doc.getName());


                        e.printStackTrace();
                    }
                }
        );

    } catch (final Exception e) {
      e.printStackTrace();
    }
*/

    }


    /*
    private List<RelationDesc> matchRelationsAgainstRule(final Document d, final ParsedRule rmr, final DocumentToSerelExpressionConverter converter) {

        final List<RelationDesc> result = new ArrayList<>();

        int sentenceIndex =1;
        for (final Sentence sentence : d.getParagraphs().get(0).getSentences()) {
            log.debug("");
            log.debug(" Sentence nr "+sentenceIndex);
            log.debug(" Sentence = " + sentence);
            sentenceIndex++;

            final SentenceMiscValues smv  = SentenceMiscValues.from(sentence);

            final Set<RelationDesc> rels1 = smv.getTokenIndexesForMatchingRelType(rmr,null);
            log.debug("rels1="+rels1);
            if(rels1.isEmpty()) {
                continue;
            }

            final Set<RelationDesc> rels2 = smv.getTokenIndexesForMatchingRelNE(rmr,rels1);
            log.debug("rels2="+rels2);
            if(rels2.isEmpty()) {
                continue;
            }

            // tutaj wiemy że typ i kotwice tych relacji są na pewno dobre

            final List<SerelExpression> serels = converter.convertAlreadyComboedFromRelDesc(rels2);

            for (final SerelExpression serel : serels) {
                log.debug("serel="+serel.getPathAsString());
                log.debug("serel="+serel.getDetailedPathAsString(true));
            }


            final Set<SerelExpression> rels3 = smv.getRelationsMatchingRule(rmr,serels);
            log.debug("rels3="+rels3);
            if(rels3.isEmpty()) {
                continue;
            }

            System.out.println("");
            System.out.println("Doc = "+d.getName());
            System.out.println("Sentence = "+sentence);
            System.out.println("Serels = "+rels3);

            rels3.forEach( r -> System.out.println(" "+sentence.getTokens().get(r.getParents1().get(0).getSourceIndex()).getOrth() + ","
                                                            +sentence.getTokens().get(r.getParents2().get(0).getSourceIndex()).getOrth() ));




        }
        return result;
    }
    
     */


}
