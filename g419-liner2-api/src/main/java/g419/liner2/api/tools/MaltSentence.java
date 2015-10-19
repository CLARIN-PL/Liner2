package g419.liner2.api.tools;

import g419.corpus.structure.*;

import java.util.*;

/**
 * Created by michal on 2/12/15.
 */

public class MaltSentence {
    private HashMap<String, String> nkjpToCoNLLPos = getnkjpToCoNLLPos();
    private final String[] maltData;
    private final HashMap<Annotation, Integer> annotationIndices;

    public MaltSentence(Sentence sent, LinkedHashSet<Annotation> sentenceAnnotations) {
        //ToDo: zagnieżdżone anotacje są pomijane
        List<String[]> coNLLTokens = convertToCoNLL(sent);
        Sentence tmpSent = sent.clone();
        tmpSent.setAnnotations(new AnnotationSet(tmpSent, sentenceAnnotations));
        int newIdx = 1;
        HashMap<Annotation, Integer> wrappedAnnotationsIndexes = new HashMap<Annotation, Integer>();
        List<String[]> wrappedTokens = new ArrayList<String[]>();
        for(int tokIdx=0; tokIdx<coNLLTokens.size(); tokIdx++){
            ArrayList<Annotation> tokenAnnotations = tmpSent.getChunksAt(tokIdx, null, true);
            if(!tokenAnnotations.isEmpty()){
                Annotation ann =  tokenAnnotations.get(0);
                if(ann.getEnd() != tokIdx){
                    int headIdx = -1;
                    for(Integer annTokIdx: ann.getTokens()){
                        if(coNLLTokens.get(annTokIdx)[4].equals("subst")) {
                            headIdx = annTokIdx;
                            break;
                        }
                    }
                    tokIdx = ann.getEnd();
                    String[] wrappedAnn;
                    if(headIdx == -1){
                        wrappedAnn = coNLLTokens.get(tokIdx);
                        wrappedAnn[3] = "ign";
                        wrappedAnn[4] = "ign";
                        wrappedAnn[5] = "_";
                    }
                    else{
                        wrappedAnn = coNLLTokens.get(headIdx);
                    }
                    wrappedAnn[0] = String.valueOf(newIdx);
                    wrappedAnn[1] = ann.getText();
                    wrappedAnn[2] = ann.getBaseText();
                    wrappedTokens.add(wrappedAnn);
                    wrappedAnnotationsIndexes.put(ann, wrappedTokens.size()-1);
                }
                else{
                    String[] token = coNLLTokens.get(tokIdx);
                    token[0] = String.valueOf(newIdx);
                    wrappedTokens.add(token);
                    wrappedAnnotationsIndexes.put(ann, newIdx-1);
                }
            }
            else{
                String[] token = coNLLTokens.get(tokIdx);
                token[0] = String.valueOf(newIdx);
                wrappedTokens.add(token);
            }
            newIdx++;
        }

        String[] dataForMalt = new String[wrappedTokens.size()];
        for(int i=0; i<wrappedTokens.size(); i++)
            dataForMalt[i] = String.join("\t", Arrays.asList(wrappedTokens.get(i)));


        this.maltData = dataForMalt;
        this.annotationIndices = wrappedAnnotationsIndexes;
    }

    public String[] getMaltData() {
        return maltData;
    }

    public HashMap<Annotation, Integer> getAnnotationIndices() {
        return annotationIndices;
    }

    private List<String[]> convertToCoNLL(Sentence sent){
        List<String[]> tokens = new ArrayList<String[]>();
        ListIterator<Token> it = sent.getTokens().listIterator();
        TokenAttributeIndex attributes = sent.getAttributeIndex();
        while (it.hasNext()) {
            String[] tokData = new String[8];
            tokData[0] = String.valueOf(it.nextIndex() + 1);

            Token token = it.next();
            tokData[1] = token.getAttributeValue(attributes.getIndex("orth"));
            tokData[2] = token.getAttributeValue(attributes.getIndex("base"));
            String ctag =  token.getAttributeValue(attributes.getIndex("ctag"));
            List<String> ctag_elements = Arrays.asList(ctag.split(":"));
            String nkjpPos = ctag_elements.get(0);

            tokData[3] = nkjpToCoNLLPos.get(nkjpPos);
            tokData[4] = nkjpPos;
            String feats = String.join("|", ctag_elements.subList(1, ctag_elements.size()));
            tokData[5] = feats.length() != 0 ? feats.toString() : "_";
            tokData[6] = "_";
            tokData[7] = "_";

            tokens.add(tokData);
        }
        return tokens;
    }


    private static HashMap<String, String> getnkjpToCoNLLPos(){
        HashMap<String, String> nkjpToCoNLLPos = new HashMap<String, String>();
        nkjpToCoNLLPos.put("bedzie", "verb");
        nkjpToCoNLLPos.put("fin", "verb");
        nkjpToCoNLLPos.put("imps", "verb");
        nkjpToCoNLLPos.put("impt", "verb");
        nkjpToCoNLLPos.put("inf", "verb");
        nkjpToCoNLLPos.put("praet", "verb");
        nkjpToCoNLLPos.put("pred", "verb");
        nkjpToCoNLLPos.put("winien", "verb");
        nkjpToCoNLLPos.put("subst", "subst");
        nkjpToCoNLLPos.put("depr", "subst");
        nkjpToCoNLLPos.put("ger", "subst");
        nkjpToCoNLLPos.put("ppron12", "subst");
        nkjpToCoNLLPos.put("ppron3", "subst");
        nkjpToCoNLLPos.put("siebie", "subst");
        nkjpToCoNLLPos.put("adj", "adj");
        nkjpToCoNLLPos.put("adja", "adj");
        nkjpToCoNLLPos.put("adjc", "adj");
        nkjpToCoNLLPos.put("adjp", "adj");
        nkjpToCoNLLPos.put("pact", "adj");
        nkjpToCoNLLPos.put("ppas", "adj");
        nkjpToCoNLLPos.put("adv", "adv");
        nkjpToCoNLLPos.put("pant", "adv");
        nkjpToCoNLLPos.put("pcon", "adv");
        nkjpToCoNLLPos.put("aglt", "aglt");
        nkjpToCoNLLPos.put("brev", "brev");
        nkjpToCoNLLPos.put("burk", "burk");
        nkjpToCoNLLPos.put("comp", "comp");
        nkjpToCoNLLPos.put("conj", "conj");
        nkjpToCoNLLPos.put("ign", "ign");
        nkjpToCoNLLPos.put("interj", "interj");
        nkjpToCoNLLPos.put("interp", "interp");
        nkjpToCoNLLPos.put("num", "num");
        nkjpToCoNLLPos.put("numcol", "numcol");
        nkjpToCoNLLPos.put("prep", "prep");
        nkjpToCoNLLPos.put("qub", "qub");
        nkjpToCoNLLPos.put("xxx", "xxx");
        return nkjpToCoNLLPos;
    }

}
