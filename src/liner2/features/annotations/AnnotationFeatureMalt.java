package liner2.features.annotations;

import com.mysql.jdbc.StringUtils;
import liner2.structure.Annotation;
import liner2.structure.Sentence;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

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


    public AnnotationFeatureMalt(String modelPath, int distance, String type) {
        try {
            malt =  new MaltParserService();

            File modelFile = new File(modelPath);
            malt.initializeParserModel(String.format("-c %s -m parse -w %s", modelFile.getName(), modelFile.getParent()));
        } catch (MaltChainedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public HashMap<Annotation, String> generate(Sentence sent) {

        convertToCoNLL(sent);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> convertToCoNLL(Sentence sent){
        List<String> tokens = new ArrayList<String>();
        ListIterator<Token> it = sent.getTokens().listIterator();
        TokenAttributeIndex attributes = sent.getAttributeIndex();
        while (it.hasNext()) {
            StringBuilder tokData = new StringBuilder();
            tokData.append(it.nextIndex() + 1 + "\t");

            Token token = it.next();
            tokData.append(token.getAttributeValue(attributes.getIndex("orth"))+"\t");
            tokData.append(token.getAttributeValue(attributes.getIndex("base"))+"\t");
            String ctag =  token.getAttributeValue(attributes.getIndex("ctag"));
            System.out.println("ctag: "+ctag);
            List<String> ctag_elements = Arrays.asList(ctag.split(":"));
            String nkjpPos = ctag_elements.get(0);

            tokData.append(nkjpToCoNLLPos.get(nkjpPos)+"\t");
            tokData.append(nkjpPos+"\t");
            StringBuilder feats = new StringBuilder();
            boolean first = true;
            for(String el: ctag_elements.subList(1,ctag_elements.size())){
                if(first)
                    first = false;
                else
                    feats.append("|");
                feats.append(el);
            }
            tokData.append(feats.length() != 0 ? feats.toString() : "_"+"\t");
            tokData.append("_"+"\t");
            tokData.append("_"+"\t");
            System.out.println(tokData.toString());

            tokens.add(tokData.toString());
        }
        return tokens;
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

