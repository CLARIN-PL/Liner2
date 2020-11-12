package g419.serel.structure;

import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class SentenceMiscValues {

    Sentence sentence;
    List<Map<String,Object>> miscValuesList = new ArrayList<>();


    public static SentenceMiscValues from(Sentence s) {

        SentenceMiscValues smv = new SentenceMiscValues();
        smv.setSentence(s);
        smv.readMiscValues(s);
        return smv;
    }

    public void readMiscValues(Sentence sentence) {

        for (int index = 0;index<sentence.getTokens().size();index++ )  {
            Token token = sentence.getTokens().get(index);

            if(token.getAttributeIndex().getIndex("misc")!=-1) {
                String misc = token.getAttributeValue("misc");
                ////System.out.println("misc = "+misc);
                if (!misc.equals("_")) {
                    ////System.out.println("HAve misc!");
                    // misc = misc.replaceAll("'","\"");
                    Map<String, Object> result = getMapFromMiscColumn(misc);
                    System.out.println("result ="+result);
                    miscValuesList.add(result);

                }
            }
        }
    }


    private Map<String,Object> getMapFromMiscColumn( String misc) {
        Map result = new HashMap<String,Object>();

        //System.out.println("misc = "+misc);

        String key=null;
        for(int i=0;i<misc.length();i++) {
            //System.out.println("chk i = "+i);
            if(misc.charAt(i)=='\'') {
                String textValue = getTextValue(i+1,misc);
                if(key==null) {
                    key=textValue;
                } else {
                    switch (key) {
                        case "bois" : result.put(key,getBoisValue(textValue)); break;
                        case "nam_rels" : result.put(key,getNamRelsValue(textValue)); break;
                    }
                    key=null;
                }
                i+=(textValue.length()+1);
            }
        }
        return result;
    }

    private String getTextValue(int i, String s) {
        int index = s.indexOf('\'',i);
        return s.substring(i,index);
    }

    private List<String> getBoisValue(String s) {
        s = s.substring(1,s.length()-1);
        String[] splits = s.split(",");
        return Arrays.asList(splits);
    }

    private List<RelationDesc> getNamRelsValue(String s) {
        s = s.substring(1,s.length()-1);
        String[] splits = s.split(",");
        return Arrays.stream(splits).map( RelationDesc::from).collect(Collectors.toList());
    }


    public Set<RelationDesc> getTokenIndexesForMatchingRelType(RuleMatchingRelations rmr, Set<RelationDesc> useOnlyRels) {
        System.out.println("Searching for type '"+rmr.getRelationType()+"'");
        Set<Integer> tokenIndexes = new HashSet<>();
        Set<RelationDesc> resultRelDesc = new HashSet<>();
        for(int i=0;i<miscValuesList.size();i++) {

            Map<String, Object> map = miscValuesList.get(i);
            List<RelationDesc> relDescs = (List<RelationDesc>) map.get("nam_rels");
            if(relDescs!= null) {
                for (RelationDesc relDesc : relDescs) {

                    if( (useOnlyRels!=null) && !useOnlyRels.contains(relDesc) ) {
                        continue;
                    }


                    System.out.println("relDesc.type = '"+relDesc.getType()+"'");
                    if (relDesc.getType().equals(rmr.getRelationType())) {
                        tokenIndexes.add(i);
                        relDesc.setSentence(this.sentence);
                        resultRelDesc.add(relDesc);
                    } else {
                        System.out.println("Rel " + relDesc + " skipped because is of type");
                    }
                }
            }
        }
        //return tokenIndexes;
        return resultRelDesc;
    }


    public Set<SerelExpression>  getRelationsMatchingRule(RuleMatchingRelations rmr, List<SerelExpression> sentenceLinksList) {
        Set<SerelExpression> result = new HashSet<>();
        for(SerelExpression se : sentenceLinksList) {
            if(isRuleMatchingSerel(rmr,se )) {
                result.add(se);
            }
        }
        return result;
    }

    private boolean isRuleMatchingSerel(RuleMatchingRelations rmr, SerelExpression se) {

        String serelPath = se.getDetailedPathAsString(false);
        System.out.println("sePAth = "+serelPath);

        StringTokenizer tokenizer = new StringTokenizer(serelPath,">|<",true);
        List<String> serelPathElements = new ArrayList<>();
        while (tokenizer.hasMoreElements()) {
            serelPathElements.add(tokenizer.nextToken().trim());
        }

        System.out.println("seTokens size = "+serelPathElements.size());
        System.out.print("seTokens= "); serelPathElements.forEach(System.out::print);
        System.out.println("");


        for(int serelPathElementIndex=0;serelPathElementIndex<serelPathElements.size();serelPathElementIndex++) {
            if (isRuleMatchingSerelPathFromTokenIndex(rmr, se, serelPathElements, serelPathElementIndex)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRuleMatchingSerelPathFromTokenIndex(RuleMatchingRelations rmr, SerelExpression se, List<String> serelPathElements, int serelPathElementIndex) {

        System.out.println("Rule :"+ rmr.rule);

        for(int ruleElementIndex=0;ruleElementIndex<rmr.getRuleElements().size();ruleElementIndex++,serelPathElementIndex++) {



            if(ruleElementIndex%2==1) {

//                // opuszczamy znaki '<' i '>'
//                ruleElementIndex++;
//                serelPathElementIndex++;

                String rElement= rmr.getRuleElements().get(ruleElementIndex).trim();
                String sElement= serelPathElements.get(serelPathElementIndex).trim();

                if(!rElement.equals(sElement))
                    return false;

                ruleElementIndex++;
                serelPathElementIndex++;

            }

            if( (ruleElementIndex==rmr.sourceRuleElementIndex ) && (serelPathElementIndex==se.getParents1().get(0).getSourceIndex()) ) {
                System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") Match! Source. ");
                continue;
            }
            if( (ruleElementIndex==rmr.targetRuleElementIndex) && (serelPathElementIndex==se.getParents2().get(0).getSourceIndex()) ) {
                System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") Match! Target. ");
                continue;
            }

            if(!rmr.isRuleElementMatchingSerelPathElement(ruleElementIndex,serelPathElements, serelPathElementIndex, se)) {
                System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") No match! RuleElement= "+rmr.getRuleElements().get(ruleElementIndex)+ " PathElement = "+serelPathElements.get(serelPathElementIndex));
                return false;
            }
            System.out.println("("+serelPathElementIndex+":"+ruleElementIndex+") Match! Token= "+ sentence.getTokens().get(serelPathElementIndex)+" ruleElement="+rmr.getRuleElements().get(ruleElementIndex));
        }
        return true;
    }





    public Set<RelationDesc> getTokenIndexesForMatchingRelNE(RuleMatchingRelations rmr, Set<RelationDesc> useOnlyRels) {
        System.out.println("Searching for NE types (and deprel if given): "+rmr);
        Set<Integer> tokenIndexes = new HashSet<>();
        Set<RelationDesc> resultRelDesc = new HashSet<>();

        String fromNEType = rmr.getSourceEntityName();
        String toNEType = rmr.getTargetEntityName();

        for(int i=0;i<miscValuesList.size();i++) {
            Map<String, Object> map = miscValuesList.get(i);
            List<RelationDesc> relDescs = (List<RelationDesc>) map.get("nam_rels");
            if(relDescs!=null) {
                for (RelationDesc relDesc : relDescs) {

                    if ((useOnlyRels != null) && !useOnlyRels.contains(relDesc)) {
                        continue;
                    }


                    if ((relDesc.getFromType().equals(fromNEType)) &&
                            (relDesc.getToType().equals(toNEType))) {

                        String ruleSourceDeprel = rmr.getRuleElementsDeprel().get(rmr.getSourceRuleElementIndex());
                        if ((ruleSourceDeprel != null) && (!ruleSourceDeprel.isEmpty())) {
                            String tokenSourceDeprel = sentence.getTokens().get(relDesc.getFromTokenIndex() - 1).getAttributeValue("deprel");
                            System.out.println("rSource='" + ruleSourceDeprel + "'  tSource='" + tokenSourceDeprel + "'");
                            if (!ruleSourceDeprel.equals(tokenSourceDeprel)) {
                                System.out.println("Rel " + relDesc + " skipped because of source deprel");
                                continue;
                            }
                        }

                        String ruleTargetDeprel = rmr.getRuleElementsDeprel().get(rmr.getTargetRuleElementIndex());
                        if ((ruleTargetDeprel != null) && (!ruleTargetDeprel.isEmpty())) {
                            String tokenTargetDeprel = sentence.getTokens().get(relDesc.getToTokenIndex() - 1).getAttributeValue("deprel");
                            System.out.println("rTarget='" + ruleTargetDeprel + "'  tTarget='" + tokenTargetDeprel + "'");

                            if (!ruleTargetDeprel.equals(tokenTargetDeprel)) {
                                System.out.println("Rel " + relDesc + " skipped because of target deprel");
                                continue;
                            }
                        }

                        tokenIndexes.add(i);
                        relDesc.setSentence(this.sentence);
                        resultRelDesc.add(relDesc);
                    } else {
                        System.out.println("Rel " + relDesc + " skipped because of NETypes");
                    }
                }
            }
        }
        //return tokenIndexes;
        return resultRelDesc;
    }





}
