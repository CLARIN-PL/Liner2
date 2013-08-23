package liner2.features;

import java.util.ArrayList;

import liner2.LinerOptions;
import liner2.features.tokens.Agr1Feature;
import liner2.features.tokens.DictFeature;
import liner2.features.tokens.Feature;
import liner2.features.tokens.TokenFeature;
import liner2.features.tokens.TokenFeatureFactory;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class TokenFeatureGenerator {

	private static ArrayList<TokenFeature> tokenGenerators = new ArrayList<TokenFeature>();
	private static ArrayList<DictFeature> sentenceGenerators = new ArrayList<DictFeature>();
	private static TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	private final static String[] sourceFeatures = new String[]{"orth", "base", "ctag"};
	private static Agr1Feature agr1Feat = null;
	private static boolean initialized = false;

    private TokenFeatureGenerator(){}

	public static void initialize(){
        if(!initialized){
            for( String sf: sourceFeatures){
                attributeIndex.addAttribute(sf);
            }
            try{
                for ( String feature : LinerOptions.get().features ){
                    Feature f = TokenFeatureFactory.create(feature);
                    if  (f != null){
                        if (DictFeature.class.isInstance(f))
                            sentenceGenerators.add((DictFeature) f);
                        else if(Agr1Feature.class.isInstance(f))
                            agr1Feat = (Agr1Feature)f;
                        else
                            tokenGenerators.add((TokenFeature) f);
                        attributeIndex.addAttribute(f.getName());
                    }
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
                System.out.println(">> " + ex.getMessage());
            }
            initialized = true;
        }
	}

    public static void updateFeatures(){
        tokenGenerators = new ArrayList<TokenFeature>();
        sentenceGenerators = new ArrayList<DictFeature>();
        attributeIndex = new TokenAttributeIndex();
        agr1Feat = null;
        initialized = false;
        initialize();

    }
	
	/**
	 * Return index of token attributes (mapping from feature name to their corresponding
	 * position in the array of attributes).
	 * @return
	 */
	public static TokenAttributeIndex getAttributeIndex(){
		return attributeIndex;
	}
	
	/**
	 * Generates feature for every token in the paragraph set. The features are added to the 
	 * token list of attributes.
	 * @param ps
	 * @throws Exception
	 */
	public static void generateFeatures(ParagraphSet ps) throws Exception {
		ps.getAttributeIndex().update(attributeIndex.allAtributes());
		for (Paragraph p : ps.getParagraphs()){
			generateFeatures(p, false);
		}
		ps.getAttributeIndex().update(LinerOptions.get().featureNames);
	}

	public static void generateFeatures(Paragraph p, boolean updateAttributeIndex) throws Exception {
		if(updateAttributeIndex)
			p.getAttributeIndex().update(attributeIndex.allAtributes());
		for (Sentence s : p.getSentences())
			generateFeatures(s);
		if(updateAttributeIndex)
			p.getAttributeIndex().update(LinerOptions.get().featureNames);
	}

	public static void generateFeatures(Sentence s) throws Exception {

		for (Token t : s.getTokens()){
			t.packAtributes(attributeIndex.getLength());
			ArrayList<Integer> toDel = new ArrayList<Integer>();
			generateFeatures(t);
			for(String sourceFeat: sourceFeatures)
				if(!LinerOptions.get().featureNames.contains(sourceFeat))
					toDel.add(attributeIndex.getIndex(sourceFeat)-toDel.size());
			for(int idx: toDel)
				t.removeAttribute(idx);
		}
		for (DictFeature f :sentenceGenerators)
			f.generate(s, attributeIndex.getIndex(f.getName()));
		if (agr1Feat != null)
			agr1Feat.generate(s, attributeIndex.getIndex("agr1"),
								 attributeIndex.getIndex("case"),
								 attributeIndex.getIndex("number"),
								 attributeIndex.getIndex("gender"));
	}

	public static void generateFeatures(Token t) throws Exception {
		for (TokenFeature f : tokenGenerators){
			t.setAttributeValue(attributeIndex.getIndex(f.getName()), f.generate(t));
		}
	}
}