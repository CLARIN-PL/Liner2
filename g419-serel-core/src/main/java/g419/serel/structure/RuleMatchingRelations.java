package g419.serel.structure;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class RuleMatchingRelations {

    String rule;

    String relationType;
    List<String> ruleElements;

    int sourceRuleElementIndex;
    String sourceEntityName;

    int targetRuleElementIndex;
    String targetEntityName;


    Map<Integer,String> ruleElementsDeprel;


    public boolean isRuleElementMatchingSerelPathElement(int ruleElementIndex, List<String> serelPathElements, int serelPathElementIndex, SerelExpression se) {
        String serelPathElement = serelPathElements.get(serelPathElementIndex).trim();
        return isRuleElementMatchingSerelPathElement(ruleElementIndex,serelPathElement);
    }


    public boolean isRuleElementMatchingSerelPathElement(int ruleElementIndex, String serelPathElement) {

        serelPathElement = serelPathElement.trim();

        String text = ruleElements.get(ruleElementIndex).trim();
        String depRel  = ruleElementsDeprel.get(ruleElementIndex).trim();

        String speText = "";
        String speDepRel = "";

        if (serelPathElement.length() > 0)
            if (serelPathElement.charAt(serelPathElement.length() - 1) == ')') {
                int indexStart = serelPathElement.lastIndexOf("(");
                if (indexStart != -1) {
                    speDepRel = serelPathElement.substring(indexStart + 1, serelPathElement.length() - 1).trim();
                    speText = serelPathElement.substring(0, indexStart).trim();
                }
            }

        if( (text!=null) && (!text.isEmpty()) && (!text.equals("*")) ) {
            if(!text.equals(speText)) {
                System.out.println(" no match for texts: "+text+" vs "+speText);
                return false;
            }
        }

        //TODO : *

        if( (depRel!=null) && (!depRel.isEmpty())  ) {
            if(!depRel.equals(speDepRel)) {
                System.out.println(" no match for depRels:"+depRel+" vs "+speDepRel);
                return false;
            }
        }

        return true;
    }


    public static RuleMatchingRelations understandRule(String rule) {

        System.out.println("Checking rule "+rule);

        RuleMatchingRelations rmr = new RuleMatchingRelations();

        rmr.rule = rule;

        String relType = rule.substring(0, rule.indexOf(':'));
        //System.out.println("relRole = "+relType);
        rmr.setRelationType(relType.trim());


        String restRule = rule.substring(rule.indexOf(':')+2);
        //System.out.println("restRule = "+restRule);


        StringTokenizer tokenizer = new StringTokenizer(restRule,">|<",true);
        List<String> ruleElements = new ArrayList<>();
        while (tokenizer.hasMoreElements()) {
            ruleElements.add(tokenizer.nextToken().trim());
        }
        int targetIndex =-1;
        int sourceIndex = -1;

        // dump ruleElements
        for (int i=0;i<ruleElements.size();i++) {
            String ruleElement = ruleElements.get(i);
            //System.out.println(i+ "\t" +ruleElement);
        }
        rmr.setRuleElements(ruleElements);


        for (int i=0;i<ruleElements.size();i++) {
            String ruleElement = ruleElements.get(i);
            if(ruleElement.indexOf(":target")!=-1) {
                ruleElement = ruleElement.replace(":target","");
                ruleElements.set(i,ruleElement.trim());
                targetIndex = i;
                rmr.setTargetRuleElementIndex(targetIndex);

                int indexStart = ruleElement.indexOf('[');
                int indexStop = ruleElement.indexOf(']');

                if( ( indexStart!= -1)  && (indexStop != -1) && (indexStart < indexStop) ) {
                    String targetEntityName = ruleElement.substring(indexStart+1, indexStop ).trim();
                    rmr.setTargetEntityName(targetEntityName);
                    ruleElement = ruleElement.substring(0,indexStart)+ruleElement.substring(indexStop+1);
                    ruleElements.set(i,ruleElement.trim());
                }
            }


            if(ruleElement.indexOf(":source")!=-1) {
                ruleElement = ruleElement.replace(":source","");
                ruleElements.set(i,ruleElement.trim());
                sourceIndex=i;
                rmr.setSourceRuleElementIndex(sourceIndex);

                int indexStart = ruleElement.indexOf('[');
                int indexStop = ruleElement.indexOf(']');

                if( ( indexStart!= -1)  && (indexStop != -1) && (indexStart < indexStop) ) {
                    String sourceEntityName = ruleElement.substring(indexStart+1, indexStop ).trim();
                    rmr.setSourceEntityName(sourceEntityName);
                    ruleElement = ruleElement.substring(0,indexStart)+ruleElement.substring(indexStop+1);
                    ruleElements.set(i,ruleElement.trim());
                }
            }
        }
        //System.out.println("targetIndex= "+targetIndex);
        //System.out.println("sourceIndex= "+sourceIndex);



        // dump ruleElements
        for (int i=0;i<ruleElements.size();i++) {
            String ruleElement = ruleElements.get(i);
            System.out.println(i+ "\t'" +ruleElement+"'");
        }

        Map<Integer,String> ruleElementsDeprel = new HashMap<>();
        for (int i=0;i<ruleElements.size();i++) {
            String ruleElement = ruleElements.get(i);
            System.out.println("RE = '" + ruleElement + "'");


            if (ruleElement.length() > 0)
                if (ruleElement.charAt(ruleElement.length() - 1) == ')') {
                    int indexStart = ruleElement.lastIndexOf("(");
                    if (indexStart != -1) {
                        String tokenDeprel = ruleElement.substring(indexStart + 1, ruleElement.length() - 1).trim();
                        ruleElement = ruleElement.substring(0, indexStart);
                        ruleElements.set(i, ruleElement.trim()); // TODO uwaga gdy to jest jednak tekst który ma tak nawiasy - czy top w ogóle możliwe ??? (bo interp)
                        ruleElementsDeprel.put(i, tokenDeprel);
                    }
                }
        }

        // dump ruleElementsDeprel
        //System.out.println(ruleElementsDeprel);
        rmr.setRuleElementsDeprel(ruleElementsDeprel);


        //System.out.println("Understanding rule "+ rule);

        // dump ruleElements
        for (int i=0;i<ruleElements.size();i++) {
            String ruleElement = ruleElements.get(i);
            //System.out.println(i+ "\t'" +ruleElement+"'");
        }


        return rmr;
    }






}
