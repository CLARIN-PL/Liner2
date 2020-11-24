package g419.serel.structure;

import g419.serel.parseRule.ParseRuleLexer;
import g419.serel.parseRule.ParseRuleParser;
import g419.serel.ruleTree.NodeMatch;
import g419.serel.ruleTree.ParseRuleListenerImpl;
import g419.serel.ruleTree.ThrowingErrorListener;
import lombok.Data;
import lombok.ToString;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

@Data
@ToString
public class ParsedRule {

    String rule;
    String relationType;
    NodeMatch rootNodeMatch;


    // method using Antlr4
    public static ParsedRule parseRule(final String rule) {

        final ParsedRule rmr = new ParsedRule();
        rmr.setRelationType(rule);

        final ParseRuleLexer lexer = new ParseRuleLexer(CharStreams.fromString(rule));
        final ParseRuleParser parser = new ParseRuleParser(new CommonTokenStream(lexer));

        final ParseRuleListenerImpl listener = new ParseRuleListenerImpl();
        parser.addParseListener(listener);
        final ThrowingErrorListener errorListener = ThrowingErrorListener.INSTANCE;
        parser.addErrorListener(errorListener);
        parser.start();

        System.out.println("STAte:" + parser.getState());

        rmr.setRelationType(listener.relationType);
        rmr.setRootNodeMatch(listener.rootNodeMatch);

        return rmr;
    }




/*
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
*/


}
