package g419.liner2.core.chunker;

import g419.corpus.structure.*;
import g419.liner2.core.features.tokens.TestRuleFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by michal on 5/22/15.
 */
public class RulesChunker extends Chunker {

  ArrayList<TestRuleFeature> rules;
  //    HashMap<String, Integer> rulesDebug;
//    HashMap<String, String> labelsDebug;
  ArrayList<String> tokensD;
  ArrayList<String> labelsD;
  ArrayList<Integer> rulesD;
  int tokensCount;
  boolean missing = false;
  Document current;

  public RulesChunker(ArrayList<TestRuleFeature> rules) {
    tokensCount = 0;
    this.rules = rules;
//        rulesDebug = new HashMap<>();
//        labelsDebug = new HashMap<>();
    tokensD = new ArrayList<>();
    labelsD = new ArrayList<>();
    rulesD = new ArrayList<>();
    try {
      Files.lines(Paths.get("/home/michal/Downloads/ga_k_2_005_covered_tokens.txt")).forEach(l -> parseLine(l));
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  private void parseLine(String line) {
    String[] attrs = line.split("\\s+");
    int ruleNr = Integer.parseInt(attrs[2].substring(attrs[2].lastIndexOf('X') + 1));
    tokensD.add(attrs[0]);
    labelsD.add(attrs[1]);
    rulesD.add(ruleNr);
  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
    HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
    missing = false;
    current = ps;
    for (Sentence sent : ps.getSentences()) {
      chunkings.put(sent, chunkSentence(sent));
    }
//        System.out.println(tokensCount);
    return chunkings;
  }

  private AnnotationSet chunkSentence(Sentence sent) {
//        for(Token t: sent.getTokens()){
//            if(!t.getOrth().equals(tokensD.get(tokensCount))){
//                System.out.println(current.getName() + " | " + tokensCount + " Wrong tok: " + t.getOrth() + " | "  + tokensD.get(tokensCount));
//                System.exit(0);
//            }
//            tokensCount++;
//        }
//        System.out.println("SENT: " + sent.getId());
    AnnotationSet chunking = new AnnotationSet(sent);
    Sentence sentCopy = sent.clone();
    HashMap<Integer, String> tokenLabels = new HashMap<>();
    HashMap<Integer, Integer> tokenRules = new HashMap<>();
    HashMap<Integer, String> debugOutput = new HashMap<>();
    int ruleNr = 0;
//        System.out.println("NUM TOKENS: " + sent.getTokenNumber());
//        for(Token t: sent.getTokens()){
////                                System.out.print(t.getOrth() + " = starts_with_lower_case: " + t.getAttributeValue("starts_with_lower_case") + " | ");
//                                System.out.print(t.getOrth() + " = has_lower_case: " + t.getAttributeValue("has_lower_case") + " | ");
//            System.out.print("suffix-2: " + t.getAttributeValue("suffix-2") + " | ");
//            System.out.print("agr1: " + t.getAttributeValue("agr1") + " | ");
//            System.out.print("suffix-1: " + t.getAttributeValue("suffix-1") + " | ");
//            System.out.print("dict_person_last_nam: " + t.getAttributeValue("dict_person_last_nam") + " | ");
////                                System.out.print("prefix-1: " + t.getAttributeValue("prefix-1") + " | ");
////                                System.out.print("dict_trigger_ext_district: " + t.getAttributeValue("dict_trigger_ext_district") + " | ");
////            System.out.print(t.getOrth() + " = all_upper: " + t.getAttributeValue("all_upper") + " | ");
////            System.out.print("has_upper_case: " + t.getAttributeValue("has_upper_case") + " | ");
////            System.out.print("orth: " + t.getAttributeValue("orth") + " | ");
////            System.out.print("no_letters: " + t.getAttributeValue("no_letters") + " | ");
////            System.out.print("prefix-3: " + t.getAttributeValue("prefix-3") + " | ");
////            System.out.print("dict_person_first_nam: " + t.getAttributeValue("dict_person_first_nam") + " | ");
////            System.out.print("dict_city_nam: " + t.getAttributeValue("dict_city_nam") + " | ");
////            System.out.print("dict_trigger_ext_geogName: " + t.getAttributeValue("dict_trigger_ext_geogName") + " | ");
//
//
//            System.out.println();
//        }
    for (TestRuleFeature rule : rules) {
      sentCopy.getAttributeIndex().addAttribute(rule.getName());
      rule.generate(sentCopy);
      List<Token> sentTokens = sentCopy.getTokens();

      for (int i = 0; i < sentTokens.size(); i++) {
        String label = sentTokens.get(i).getAttributeValue(rule.getName());
//                System.out.println(sentTokens.get(i).getOrth() +  " LABEL " + label);
        if (!tokenLabels.containsKey(i) && !label.equals("0")) {
          StringBuilder sb = new StringBuilder();
          sb.append(label + " | " + rule.rule + "\n");

          debugOutput.put(i, sb.toString());
          String orth = sentTokens.get(i).getOrth();
          int tokidx = tokensCount + i;
//                    if(tokensD.get(tokidx).equals(orth)){
//                        if(rulesD.get(tokidx) != ruleNr){
//                            TestRuleFeature properRule = rules.get(rulesD.get(tokidx));
//                            System.out.println(tokidx + " WRONG RULE " + rule.rule + " (" + (ruleNr + 1) + ")" );
//                            System.out.println("should be " + properRule.rule + " (" + (rulesD.get(tokidx) +  + 1) + ")");
//                            boolean outa = false;
//                            boolean outp = false;
//                            for(int j=0; j<rule.sourceFeats.size(); j++){
//                                sb.append(rule.sourceFeats.get(j) + "[" + rule.offsets.get(j) + "]=");
//                                int srcIdx = i + rule.offsets.get(j);
////                        System.out.println(srcIdx + " | " + (srcIdx < 0  || srcIdx >=sentTokens.size()));
//                                if(srcIdx < 0  || srcIdx >=sentTokens.size()){
////                                    System.out.println("OUT OF RANGE OUR RULE");
//                                    outa = true;
//                                }
//                            }
//
//                            for(int j=0; j<properRule.sourceFeats.size(); j++){
//                                sb.append(properRule.sourceFeats.get(j) + "[" + properRule.offsets.get(j) + "]=");
//                                int srcIdx = i + properRule.offsets.get(j);
////                        System.out.println(srcIdx + " | " + (srcIdx < 0  || srcIdx >=sentTokens.size()));
//                                if(srcIdx < 0  || srcIdx >=sentTokens.size()){
//                                    System.out.println("OUT OF RANGE PROPER RULE");
//                                    outp = true;
//                                }
//                            }
//                            if(!outa){
//                                System.out.println("RANGE OK");
//;                                System.exit(0);
//                            }
//
////                            for(Token t: sentTokens){
//////                                System.out.print(t.getOrth() + " = starts_with_lower_case: " + t.getAttributeValue("starts_with_lower_case") + " | ");
//////                                System.out.print("has_lower_case: " + t.getAttributeValue("has_lower_case") + " | ");
//////                                System.out.print("prefix-1: " + t.getAttributeValue("prefix-1") + " | ");
//////                                System.out.print("dict_trigger_ext_district: " + t.getAttributeValue("dict_trigger_ext_district") + " | ");
////                                System.out.print(t.getOrth() + " = all_upper: " + t.getAttributeValue("all_upper") + " | ");
////                                System.out.print("has_upper_case: " + t.getAttributeValue("has_upper_case") + " | ");
////                                System.out.print("orth: " + t.getAttributeValue("orth") + " | ");
////                                System.out.print("no_letters: " + t.getAttributeValue("no_letters") + " | ");
////                                System.out.print("prefix-3: " + t.getAttributeValue("prefix-3") + " | ");
////                                System.out.print("dict_person_first_nam: " + t.getAttributeValue("dict_person_first_nam") + " | ");
////                                System.out.print("dict_city_nam: " + t.getAttributeValue("dict_city_nam") + " | ");
////                                System.out.print("dict_trigger_ext_geogName: " + t.getAttributeValue("dict_trigger_ext_geogName") + " | ");
////
////
////                                System.out.println();
////                            }
////                            System.exit(0);
//                        }
//                        else{
////                            System.out.println("OK");
//                        }
//
//
////                        if(rulesDebug.get(orth) != ruleNr){
////                            System.out.println(sentCopy.toString());
////                            TestRuleFeature proper = rules.get(rulesDebug.get(orth));
////                            System.out.println(orth + " | " + rule.rule + "(" + ruleNr + ") | WRONG RULE, SHOULD BE:  " + proper.rule + "(" + rulesDebug.get(orth) + ")");
////                            sentCopy.getAttributeIndex().addAttribute(proper.getName());
////                            proper.generate(sentCopy);
////                            String label2 = sentTokens.get(i).getAttributeValue(proper.getName());
////                            System.out.println("PROPER RULE LABEL: " + label2);
////                            System.exit(1);
////                        }
////                        if(!labelsDebug.get(orth).equals(label)){
////                            System.out.println("WRONG LABEL");
////                            System.exit(1);
////                        }
//                    }
//                    else{
////                        System.out.println(orth + " | " + tokensD.get(tokensCount) + "wrong token on: " + tokensCount + " | " + current.getName());
//
//                        if(!missing){
//                            System.out.println(current.getName());
//                            missing = true;
//                        }
//                    }
          tokenLabels.put(i, label);
          tokenRules.put(i, ruleNr);
        }
      }
      ruleNr++;
//            for(int i: tokenLabels.keySet()){
//                System.out.println(i + " | " + tokenLabels.get(i));
//            }
//            System.exit(0);
    }
    List<Token> sentTokens = sent.getTokens();
    for (int i = 0; i < sentTokens.size(); i++) {
      int ourRule = tokenRules.get(i);
      int properRule = rulesD.get(tokensCount + i);
      if (ourRule != properRule) {
        System.out.println(tokensCount + i + "\t" + sentTokens.get(i).getOrth() + "\t" + ourRule + "\t" + properRule);
      }

    }
//        System.exit(0);
    if (!missing) {
      tokensCount += sent.getTokenNumber();
    }
//        for(int i=0; i<sent.getTokenNumber(); i++){
//            String output = debugOutput.containsKey(i) ? debugOutput.get(i) : "NO MATCH";
//            System.out.println(sent.getTokens().get(i).getOrth() + " | " + output);
//        }

    Annotation ann = null;
    for (int i = 0; i < sentTokens.size(); i++) {
      if (tokenLabels.containsKey(i)) {
        String label = tokenLabels.get(i);
        if (label.startsWith("B")) {
          if (ann != null) {
            chunking.addChunk(ann);
//                        System.out.println(ann.getText());
          }
          ann = new Annotation(i, label.substring(2), sent);
        } else if (label.startsWith("I")) {
          if (ann != null) {
            ann.addToken(i);
          } else {
            ann = new Annotation(i, label.substring(2), sent);
          }
        } else if (label.equals("O")) {
          if (ann != null) {
            chunking.addChunk(ann);
//                        System.out.println(ann.getText());
            ann = null;
          }
        }
      } else {
        System.out.println("NO RULE MATCHED");
        if (ann != null) {
          chunking.addChunk(ann);
//                    System.out.println(ann.getText());
          ann = null;
        }
      }
    }
    if (ann != null) {
      chunking.addChunk(ann);
//            System.out.println(ann.getText());
    }

//        for(Annotation a: chunking.chunkSet()){
//            System.out.println(a.getText());
//        }
    return chunking;
  }
}
