package g419.serel.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.serel.structure.RuleMatchingRelations;
import g419.serel.structure.SentenceMiscValues;
import org.apache.commons.cli.CommandLine;

import java.util.*;

public class ActionMatchRelations extends Action {

  private String inputFilename;
  private String inputFormat;

  public ActionMatchRelations() {
    super("match-relations");
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

      // z opisu
    //String searchingRule = "location:: w (case)> [nam_loc_gpe_country] (nmod) :target > * < [nam_loc_gpe_city] (nmod) :source";

      // 1 bez deprel
    //String searchingRule =" location:: [nam_fac_goe] :source > * > [nam_loc_gpe_city] :target ";

        // 2 z deprel dobrym
      String searchingRule =" location:: [nam_fac_goe] (root) :source > * > [nam_loc_gpe_city] (nmod) :target ";

      // 3 z deprel złym
     // String searchingRule =" location:: [nam_fac_goe] (root) :source > * > [nam_loc_gpe_city] (case) :target ";


    RuleMatchingRelations rmr = RuleMatchingRelations.understandRule(searchingRule);
    System.out.println(searchingRule);
    System.out.println("Understood rule ="+rmr);


    //System.out.println("Starting reading ...");
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         ) {
      //System.out.println("Isnide try");
      reader.forEach(d -> matchRelationsAgainstRule(d, rmr));
    } catch (Exception e) {
      e.printStackTrace();
    }


  }


  private List<RelationDesc> matchRelationsAgainstRule(Document d, RuleMatchingRelations rmr) {

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

          // tutaj wiemy że kotwice relacji są na pewno dobre

          Set<RelationDesc> rels3 = smv.getRelationsMatchingRule(rmr,rels2);
          System.out.println("rels3="+rels3);
          if(rels3.isEmpty())
              continue;



      }

      return result;

  }



  private void decomposeRelation(String rel) {
    //String


  }




}
