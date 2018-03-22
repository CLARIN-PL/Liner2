package g419.liner2.core.tools.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.structure.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MaltSentence {
    private HashMap<String, String> nkjpToCoNLLPos = getnkjpToCoNLLPos();
    private String[] maltData = null;
    private Set<Annotation> annotations = Sets.newHashSet();
    List<MaltSentenceLink> links = Lists.newArrayList();
    private Sentence sentence = null;

    public MaltSentence(final Sentence sent) {
        maltData = convertToCoNLL(sent).stream()
                .map(r->String.join("\t", Arrays.asList(r)))
                .toArray(String[]::new);
        annotations = sent.getChunks();
        sentence = sent;
    }

    public MaltSentence(final Sentence sent, final List<Pattern> patterns) {
        this(TokenWrapper.wrapAnnotations(sent, patterns));
    }

    public void setMaltDataAndLinks(final String[] output) {
        maltData = output;
        setLinks(Arrays.stream(output)
                .map(line->line.split("\t"))
                .map(parts->new MaltSentenceLink(links.size(), Integer.parseInt(parts[8]) - 1, parts[9]))
                .collect(Collectors.toList()));
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
    	return index >= this.links.size() ? null : this.links.get(index);
    }

    /**
     * Zwraca listę linków wskazujących na token o wskazanym indeksie.
     * @param index
     * @return
     */
    public List<MaltSentenceLink> getLinksByTargetIndex(int index){
        return links.stream().filter(link->link.getTargetIndex()==index).collect(Collectors.toList());
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    private List<String[]> convertToCoNLL(Sentence sent){
        List<String[]> tokens = Lists.newArrayList();
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
        HashMap<String, String> nkjpToCoNLLPos = Maps.newHashMap();
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
