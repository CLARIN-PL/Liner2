package g419.liner2.core.tools.parser;

import g419.corpus.structure.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by michal on 2/12/15.
 */

public class MaltSentence {
    private HashMap<String, String> nkjpToCoNLLPos = getnkjpToCoNLLPos();
    private String[] maltData = null;
    private LinkedHashSet<Annotation> annotations = new LinkedHashSet<Annotation>();
    //Pattern allAnnotationsPattern = Pattern.compile("nam.*");
    List<MaltSentenceLink> links = new ArrayList<MaltSentenceLink>();
    private Sentence sentence = null;

    public MaltSentence(Sentence sent) {
        List<String[]> coNLLTokens = convertToCoNLL(sent);

        String[] dataForMalt = new String[coNLLTokens.size()];
        for(int i=0; i<coNLLTokens.size(); i++)
            dataForMalt[i] = String.join("\t", Arrays.asList(coNLLTokens.get(i)));

        this.maltData = dataForMalt;
        this.sentence = sent;
    }

    public MaltSentence(Sentence sent, Set<Annotation> sentenceAnnotations) {

    	List<Pattern> patterns = new ArrayList<Pattern>();
    	//patterns.add(this.allAnnotationsPattern);
    	
        Sentence wrappedSent = TokenWrapper.wrapAnnotations(sent, patterns);        
        List<String[]> coNLLTokens = convertToCoNLL(wrappedSent);

        String[] dataForMalt = new String[coNLLTokens.size()];
        for(int i=0; i<coNLLTokens.size(); i++)
            dataForMalt[i] = String.join("\t", Arrays.asList(coNLLTokens.get(i)));

        this.maltData = dataForMalt;
        this.annotations = wrappedSent.getChunks();
        this.sentence = wrappedSent;
    }


	public void setMaltData(String[] output) {
		this.maltData = output;
	}

    public String[] getMaltData() {
        return maltData;
    }
    
    public Sentence getSentence() {
    	return this.sentence;
    }
    
    public void setLinks(List<MaltSentenceLink> links){
    	this.links = links;
    }
    
    public MaltSentenceLink getLink(int index){
    	if ( index >= this.links.size() ){
    		return null;
    	}
    	else{
    		return this.links.get(index);
    	}
    }

    public void wrapConjunctions(){
//        for(int i=0; i<sentenceData.length; i++){
//            if(sentenceData[i][3].equals("conj") && Integer.parseInt(sentenceData[i][8]) != 0){
//                for(int j=0; j<sentenceData.length; j++){
//                    if(sentenceData[j][9].equals("conjunct") && (Integer.parseInt(sentenceData[j][8]) - 1) == i){
//                        sentenceData[j][9] = sentenceData[i][9];
//                        sentenceData[j][8] = sentenceData[i][8];
//                    }
//                }
//                sentenceData[i][9] = "deleted_rel";
//            }
//        }
    }
    

    /**
     * Zwraca listę linków wskazujących na token o wskazanym indeksie.
     * @param index
     * @return
     */
    public List<MaltSentenceLink> getLinksByTargetIndex(int index){
    	List<MaltSentenceLink> links = new ArrayList<MaltSentenceLink>();
    	for ( MaltSentenceLink link : this.links){
    		if ( link.getTargetIndex() == index ){
    			links.add(link);
    		}
    	}
    	return links;
    }

    public HashSet<Annotation> getAnnotations() {
        return annotations;
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
