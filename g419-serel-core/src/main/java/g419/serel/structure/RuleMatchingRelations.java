package g419.serel.structure;

import g419.serel.ruleMatcher.SerelRuleMatcherLexer;
import g419.serel.ruleMatcher.SerelRuleMatcherParser;
import lombok.Data;
import lombok.ToString;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

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


    public boolean isRuleElementMatchingSerelPathElement(final int ruleElementIndex, final List<String> serelPathElements, final int serelPathElementIndex, final SerelExpression se) {
        final String serelPathElement = serelPathElements.get(serelPathElementIndex).trim();
        //System.out.println("serelPathElement = "+serelPathElement);
        return isRuleElementMatchingSerelPathElement(ruleElementIndex,serelPathElement);
    }

    public boolean isRuleElementMatchingSerelPathElement(final int ruleElementIndex, final String serelPathElement) {
        return isRuleElementMatchingSerelPathElement(ruleElementIndex, serelPathElement, false);
    }

    public boolean isRuleElementMatchingSerelPathElement(final int ruleElementIndex, String serelPathElement, final boolean starMode ) {

        //System.out.println("isREMSPE: ruleElementIndex = "+ruleElementIndex);

        serelPathElement = serelPathElement.trim();

        final String text = ruleElements.get(ruleElementIndex).trim();
        final String depRel  = ruleElementsDeprel.get(ruleElementIndex).trim();

        String speText = "";
        String speDepRel = "";

        if (serelPathElement.length() > 0) {
            if (serelPathElement.charAt(serelPathElement.length() - 1) == ')') {
                final int indexStart = serelPathElement.lastIndexOf("(");
                if (indexStart != -1) {
                    speDepRel = serelPathElement.substring(indexStart + 1, serelPathElement.length() - 1).trim();
                    speText = serelPathElement.substring(0, indexStart).trim();
                }
            }
        }

        if( (text!=null) && (!text.isEmpty()) && (!text.equals("*")) ) {
            if(!text.equals(speText)) {
                //System.out.println(" no match for texts: "+text+" vs "+speText);
                return false;
            }
        }

        //TODO : *

        if( (depRel!=null) && (!depRel.isEmpty())  ) {
            if(!depRel.equals(speDepRel)) {
                //System.out.println(" no match for depRels:"+depRel+" vs "+speDepRel);
                return false;
            }
        }

        return true;
    }

    // method using Antlr4
    public static RuleMatchingRelations understandRule(final String rule) {
        final RuleMatchingRelations rmr = new RuleMatchingRelations();

        final SerelRuleMatcherLexer lexer = new SerelRuleMatcherLexer(CharStreams.fromString(rule));
        final SerelRuleMatcherParser parser = new SerelRuleMatcherParser(new CommonTokenStream(lexer));

        parser.start();


        return rmr;
    }



    // method using old -way
    public static RuleMatchingRelations understandRuleOld(final String rule) {

        //System.out.println("Checking rule "+rule);

        final RuleMatchingRelations rmr = new RuleMatchingRelations();

        rmr.rule = rule;

        final String relType = rule.substring(0, rule.indexOf(':'));
        //System.out.println("relRole = "+relType);
        rmr.setRelationType(relType.trim());


        final String restRule = rule.substring(rule.indexOf(':')+2);
        //System.out.println("restRule = "+restRule);


        final StringTokenizer tokenizer = new StringTokenizer(restRule,">|<",true);
        final List<String> ruleElements = new ArrayList<>();
        while (tokenizer.hasMoreElements()) {
            ruleElements.add(tokenizer.nextToken().trim());
        }
        int targetIndex =-1;
        int sourceIndex = -1;

        // dump ruleElements
        for (int i=0;i<ruleElements.size();i++) {
            final String ruleElement = ruleElements.get(i);
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

                final int indexStart = ruleElement.indexOf('[');
                final int indexStop = ruleElement.indexOf(']');

                if( ( indexStart!= -1)  && (indexStop != -1) && (indexStart < indexStop) ) {
                    final String targetEntityName = ruleElement.substring(indexStart+1, indexStop ).trim();
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

                final int indexStart = ruleElement.indexOf('[');
                final int indexStop = ruleElement.indexOf(']');

                if( ( indexStart!= -1)  && (indexStop != -1) && (indexStart < indexStop) ) {
                    final String sourceEntityName = ruleElement.substring(indexStart+1, indexStop ).trim();
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
            final String ruleElement = ruleElements.get(i);
            //System.out.println(i+ "\t'" +ruleElement+"'");
        }

        final Map<Integer,String> ruleElementsDeprel = new HashMap<>();
        for (int i=0;i<ruleElements.size();i++) {
            String ruleElement = ruleElements.get(i);
            //System.out.println("RE = '" + ruleElement + "'");


            if (ruleElement.length() > 0) {
                if (ruleElement.charAt(ruleElement.length() - 1) == ')') {
                    final int indexStart = ruleElement.lastIndexOf("(");
                    if (indexStart != -1) {
                        final String tokenDeprel = ruleElement.substring(indexStart + 1, ruleElement.length() - 1).trim();
                        ruleElement = ruleElement.substring(0, indexStart);
                        ruleElements.set(i, ruleElement.trim()); // TODO uwaga gdy to jest jednak tekst który ma tak nawiasy - czy top w ogóle możliwe ??? (bo interp)
                        ruleElementsDeprel.put(i, tokenDeprel);
                    }
                }
            }
        }

        // dump ruleElementsDeprel
        //System.out.println(ruleElementsDeprel);
        rmr.setRuleElementsDeprel(ruleElementsDeprel);


        //System.out.println("Understanding rule "+ rule);

        // dump ruleElements
        for (int i=0;i<ruleElements.size();i++) {
            final String ruleElement = ruleElements.get(i);
            //System.out.println(i+ "\t'" +ruleElement+"'");
        }


        return rmr;
    }






}
