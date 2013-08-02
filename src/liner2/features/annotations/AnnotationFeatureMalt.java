package liner2.features.annotations;

import com.mysql.jdbc.StringUtils;
import liner2.structure.Annotation;
import liner2.structure.Sentence;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 7/30/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationFeatureMalt extends AnnotationSentenceFeature {

    private MaltParserService malt;
    private HashMap<String, String> nkjpToCoNLLPos = getnkjpToCoNLLPos();
    private String type;
    private int distance;


    public AnnotationFeatureMalt(String modelPath, int distance, String type) {
        this.type = type;
        this.distance = distance;
        try {
            malt =  new MaltParserService();

            File modelFile = new File(modelPath);
            malt.initializeParserModel(String.format("-c %s -m parse -w %s", modelFile.getName(), modelFile.getParent()));
        } catch (MaltChainedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public HashMap<Annotation, String> generate(Sentence sent) {

        List<String[]> coNLLTokens = convertToCoNLL(sent);
        HashMap<Integer, Annotation> annotatedTokens = new HashMap<Integer, Annotation>();
        for( Annotation ann: sent.getChunks()){
            if(ann.getBegin() != ann.getEnd())
                annotatedTokens.put(ann.getBegin(), ann);
        }
        int newIdx = 1;
        HashMap<Annotation, Integer> wrappedAnnotationsIndexes = new HashMap<Annotation, Integer>();
        List<String[]> wrappedTokens = new ArrayList<String[]>();
        for(int tokIdx=0; tokIdx<coNLLTokens.size(); tokIdx++){
            if(annotatedTokens.containsKey(tokIdx)){
                List<String> tokens = new ArrayList<String>();
                Annotation ann =  annotatedTokens.get(tokIdx);
                boolean foundHead = false;
                int headIdx = tokIdx;
                for(Integer annTokIdx: ann.getTokens())
                    if(!foundHead && coNLLTokens.get(annTokIdx)[4].equals("subst")){
                        headIdx = tokIdx;
                        foundHead =true;
                    }
                tokIdx = ann.getEnd();

                String[] wrappedAnn = coNLLTokens.get(headIdx);
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
            }
            newIdx++;
        }

        String[] dataForMalt = new String[wrappedTokens.size()];
        for(int i=0; i<wrappedTokens.size(); i++)
            dataForMalt[i] = join(Arrays.asList(wrappedTokens.get(i)), "\t");

        HashMap<Annotation, String> features = new HashMap<Annotation, String>();
        try {
            String [] parsedTokens =  malt.parseTokens(dataForMalt);

            for(Annotation ann: wrappedAnnotationsIndexes.keySet()){
                String annData = parsedTokens[wrappedAnnotationsIndexes.get(ann)];
                features.put(ann, getFeature(annData, parsedTokens));

            }
        } catch (MaltChainedException e) {
            e.printStackTrace();
        }
        return features;
    }

    public String getFeature(String annotation, String[] maltData){
        if(this.distance > 1){
            int i=1;
            int parentIdx = Integer.parseInt(annotation.split("\t")[8]) - 1;
            while(i<distance){
                if(parentIdx<0)
                    return "NULL";
                annotation =  maltData[parentIdx];
                parentIdx = Integer.parseInt(annotation.split("\t")[8]) - 1;
                i++;
            }
        }
        if(this.type.equals("base"))
            return getParentBase(annotation, maltData);
        else if(this.type.equals("relation"))
            return getRelation(annotation);
        return null;
    }

    public String getRelation(String annotation){
        return annotation.split("\t")[9];
    }

    public String getParentBase(String annotation, String[] maltData){
        int parentIdx = Integer.parseInt(annotation.split("\t")[8]) - 1;
        if(parentIdx < 0)
            return "NULL";
        return  maltData[parentIdx].split("\t")[2];
    }

    public List<String[]> convertToCoNLL(Sentence sent){
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
            String feats = join(ctag_elements.subList(1,ctag_elements.size()), "|");
            tokData[5] = feats.length() != 0 ? feats.toString() : "_";
            tokData[6] = "_";
            tokData[7] = "_";

            tokens.add(tokData);
        }
        return tokens;
    }

    public String join(List<String> sequence, String delimiter){
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for(String el: sequence){
            if(first)
                first = false;
            else
                out.append(delimiter);
            out.append(el);
        }
        return out.toString();
    }

    public static HashMap<String, String> getnkjpToCoNLLPos(){
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

