package g419.serel.action;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.serel.structure.RuleMatchingRelations;
import org.apache.commons.cli.CommandLine;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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

    String rule = "location:: w (case)> [nam_loc_gpe_country] (nmod) :target > * < [nam_loc_gpe_city] (nmod) :source";

    RuleMatchingRelations rmr = understandRule(rule);



    //System.out.println("Starting reading ...");
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
         ) {
      //System.out.println("Isnide try");
      reader.forEach(d -> matchRelationsAgainstRule(d, rmr));
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  private RuleMatchingRelations understandRule(String rule) {

    RuleMatchingRelations rmr = new RuleMatchingRelations();

    String relType = rule.substring(0, rule.indexOf(':'));
    //System.out.println("relRole = "+relType);
    rmr.setRelationType(relType);


    String restRule = rule.substring(rule.indexOf(':')+2);
    //System.out.println("restRule = "+restRule);


    StringTokenizer tokenizer = new StringTokenizer(restRule,">|<",true);
    List<String> tokens = new ArrayList<>();
    while (tokenizer.hasMoreElements()) {
      tokens.add(tokenizer.nextToken().trim());
    }
    int targetIndex =-1;
    int sourceIndex = -1;

    // dump tokens
    for (int i=0;i<tokens.size();i++) {
      String token = tokens.get(i);
      //System.out.println(i+ "\t" +token);
    }
    rmr.setTokens(tokens);


    for (int i=0;i<tokens.size();i++) {
      String token = tokens.get(i);
      if(token.indexOf(":target")!=-1) {
        token = token.replace(":target","");
        tokens.set(i,token.trim());
        targetIndex = i;
        rmr.setTargetIndex(targetIndex);

        int indexStart = token.indexOf('[');
        int indexStop = token.indexOf(']');

        if( ( indexStart!= -1)  && (indexStop != -1) && (indexStart < indexStop) ) {
          String targetEntityName = token.substring(indexStart+1, indexStop - 1).trim();
          rmr.setTargetEntityName(targetEntityName);
          token = token.substring(0,indexStart)+token.substring(indexStop+1);
          tokens.set(i,token.trim());
        }
      }


      if(token.indexOf(":source")!=-1) {
        token = token.replace(":source","");
        tokens.set(i,token.trim());
        sourceIndex=i;
        rmr.setSourceIndex(sourceIndex);

        int indexStart = token.indexOf('[');
        int indexStop = token.indexOf(']');

        if( ( indexStart!= -1)  && (indexStop != -1) && (indexStart < indexStop) ) {
          String sourceEntityName = token.substring(indexStart+1, indexStop - 1).trim();
          rmr.setSourceEntityName(sourceEntityName);
          token = token.substring(0,indexStart)+token.substring(indexStop+1);
          tokens.set(i,token.trim());
        }
      }
    }
    //System.out.println("targetIndex= "+targetIndex);
    //System.out.println("sourceIndex= "+sourceIndex);



    // dump tokens
    for (int i=0;i<tokens.size();i++) {
      String token = tokens.get(i);
      //System.out.println(i+ "\t'" +token+"'");
    }

    Map<Integer,String> tokenCases = new HashMap<>();
    for (int i=0;i<tokens.size();i++) {
      String token = tokens.get(i);
      if (token.charAt(token.length() - 1) == ')') {
        int indexStart = token.lastIndexOf("(");
        if (indexStart != -1) {
          String tokenCase = token.substring(indexStart+1, token.length() - 1).trim();
          token = token.substring(0,indexStart);
          tokens.set(i,token.trim()); // TODO uwaga gdy to jest jednak tekst który ma tak nawiasy - czy top w ogóle możliwe ??? (bo interp)
          tokenCases.put(i, tokenCase);
        }
      }
    }

    // dump tokenCases
    //System.out.println(tokenCases);
    rmr.setTokenCases(tokenCases);


    //System.out.println("Understanding rule "+ rule);

    // dump tokens
    for (int i=0;i<tokens.size();i++) {
      String token = tokens.get(i);
      //System.out.println(i+ "\t'" +token+"'");
    }


    return rmr;
  }

  private void matchRelationsAgainstRule(Document d, RuleMatchingRelations rmr) {

    int index =1;
    for (Sentence sentence : d.getParagraphs().get(0).getSentences()) {
        ////System.out.println(" Sentence nr "+index);
        index++;
        for (Token token : sentence.getTokens()) {
          if(token.getAttributeIndex().getIndex("misc")!=-1) {
            String misc= token.getAttributeValue("misc");
            ////System.out.println("misc = "+misc);
            if(!misc.equals("_")) {
              ////System.out.println("HAve misc!");
              misc = misc.replaceAll("'","\"");
              try {
                Map<String, Object> result =
                        new ObjectMapper().readValue(misc, HashMap.class);

                ////System.out.println("result ="+result);

                for (String s : result.keySet()) {
                  //System.out.println( d.getName()+ ": sentence "+index + " : "+s);
                }
              } catch (JsonParseException e) {
                e.printStackTrace();
              } catch (JsonMappingException e) {
                e.printStackTrace();
              } catch (IOException e) {
                e.printStackTrace();
              }

            }
          }
        }
//        //System.out.println(" Sentence tokens props = "+sentence.getTokens());

    }
//    getLogger().error(" Relations set size :"+d.getRelationsSet().size());
//    for( Relation rel : d.getRelationsSet()) {
//         getLogger().error(" Rel from= "+rel.getAnnotationFrom().getBaseText() +"  to = "+rel.getAnnotationTo().getBaseText());
//    }
  }

  private void decomposeRelation(String rel) {
    //String


  }

}
