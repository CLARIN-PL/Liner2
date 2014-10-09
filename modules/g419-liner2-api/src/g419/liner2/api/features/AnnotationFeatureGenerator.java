package g419.liner2.api.features;

import g419.corpus.structure.*;
import g419.liner2.api.features.annotations.AnnotationFeature;
import g419.liner2.api.features.annotations.AnnotationFeatureClosestBase;
import g419.liner2.api.features.annotations.AnnotationFeatureContextBase;
import g419.liner2.api.features.annotations.AnnotationFeatureDict;
import g419.liner2.api.features.annotations.AnnotationFeatureMalt;
import g419.liner2.api.features.annotations.AnnotationFeatureNeFirstBase;
import g419.liner2.api.features.annotations.AnnotationSentenceFeature;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AnnotationFeatureGenerator {

	private List<AnnotationFeature> features = new ArrayList<AnnotationFeature>();
    private List<AnnotationFeatureMalt> maltFeatures = new ArrayList<AnnotationFeatureMalt>();
    private List<AnnotationSentenceFeature> sentenceFeatures = new ArrayList<AnnotationSentenceFeature>();
    private HashMap<String, String> nkjpToCoNLLPos = getnkjpToCoNLLPos();

	private Pattern patternBase = Pattern.compile("base:(-?[0-9]*)$");
    private Pattern patternDict = Pattern.compile("dict:([^:]*):([^:]*)$");
    private Pattern patternMalt = Pattern.compile("malt:([^:]*):([0-9]*):(base|relation)$");
    private Pattern patternClosestBase = Pattern.compile("closest-base:(-?[0-9]*):([a-z]+)$");
    private Pattern patternNeFirstBase = Pattern.compile("ne-first-base:(-?[0-9]*):([a-z]+)$");

	/**
	 *
	 * @param features — array with feature definitions
	 */
	public AnnotationFeatureGenerator(List<String> features){
		for ( String feature : features ){
			Matcher matcherBase = this.patternBase.matcher(feature);
			if ( matcherBase.find() ){
				this.features.add(new AnnotationFeatureContextBase(Integer.parseInt(matcherBase.group(1))));
                continue;
			}
            Matcher matcherDict = this.patternDict.matcher(feature);
            if ( matcherDict.find() ){
                this.features.add(new AnnotationFeatureDict(matcherDict.group(2), matcherDict.group(1)));
                continue;
            }
            Matcher matcherMalt = this.patternMalt.matcher(feature);
            if ( matcherMalt.find() ){
                this.maltFeatures.add(new AnnotationFeatureMalt(matcherMalt.group(1), Integer.parseInt(matcherMalt.group(2)), matcherMalt.group(3)));
                continue;
            }
            Matcher matcherClosestBase = this.patternClosestBase.matcher(feature);
            if ( matcherClosestBase.find() ){
                this.sentenceFeatures.add(new AnnotationFeatureClosestBase(matcherClosestBase.group(2), Integer.parseInt(matcherClosestBase.group(1))));
                continue;
            }
            Matcher matcherNeFirstBase = this.patternNeFirstBase.matcher(feature);
            if ( matcherNeFirstBase.find() ){
                this.sentenceFeatures.add(new AnnotationFeatureNeFirstBase(matcherNeFirstBase.group(2), Integer.parseInt(matcherNeFirstBase.group(1))));
            }
		}
	}

	public List<String> generate(Annotation ann){
		List<String> features = new ArrayList<String>();
		for (AnnotationFeature afg : this.features)
			features.add(afg.generate(ann));
		return features;
	}

    public List<HashMap<Annotation,String>> generate(Sentence sent, LinkedHashSet<Annotation> sentenceAnnotations){
        List<HashMap<Annotation,String>> features = new ArrayList<HashMap<Annotation, String>>();
        MaltFeatureSentence maltSent;
        if(!this.maltFeatures.isEmpty()){
            maltSent = prepareSentenceForMaltparser(sent, sentenceAnnotations);
            for (AnnotationFeatureMalt afg : this.maltFeatures)
                features.add(afg.generate(maltSent.getMaltData(), maltSent.getAnnotationIndices()));
        }
        for (AnnotationSentenceFeature afg : this.sentenceFeatures)
            features.add(afg.generate(sent, sentenceAnnotations));
        return features;
    }

	public int getFeaturesCount(){
		return this.features.size()+this.maltFeatures.size()+this.sentenceFeatures.size();
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

    public MaltFeatureSentence prepareSentenceForMaltparser(Sentence sent, LinkedHashSet<Annotation> sentenceAnnotations){
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
                    int headIdx = tokIdx;
                    for(Integer annTokIdx: ann.getTokens())
                        if(coNLLTokens.get(annTokIdx)[4].equals("subst")){
                            headIdx = tokIdx;
                            break;
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
            dataForMalt[i] = join(Arrays.asList(wrappedTokens.get(i)), "\t");
        return new MaltFeatureSentence(dataForMalt, wrappedAnnotationsIndexes);
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

    final class MaltFeatureSentence {
        private final String[] maltData;
        private final HashMap<Annotation, Integer> annotationIndices;

        public MaltFeatureSentence(String[] maltData, HashMap<Annotation, Integer> annotationIndices) {
            this.maltData = maltData;
            this.annotationIndices = annotationIndices;
        }

        public String[] getMaltData() {
            return maltData;
        }

        public HashMap<Annotation, Integer> getAnnotationIndices() {
            return annotationIndices;
        }
    }



}