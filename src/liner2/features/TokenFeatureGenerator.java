package liner2.features;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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

	private ArrayList<TokenFeature> tokenGenerators = new ArrayList<TokenFeature>();
	private ArrayList<DictFeature> sentenceGenerators = new ArrayList<DictFeature>();
	private TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	private String[] sourceFeatures = new String[]{"orth", "base", "ctag"};
	private Agr1Feature agr1Feat = null;
	private ArrayList<String> featureNames;
	/**
	 * 
	 * @param features — array with feature definitions
	 */
	public TokenFeatureGenerator(LinkedHashMap<String, String> features){
        this.featureNames = new ArrayList<String>(features.keySet());
		for( String sf: sourceFeatures)
			this.attributeIndex.addAttribute(sf);
		try{
			for ( String feature : features.values() ){
				Feature f = TokenFeatureFactory.create(feature);
				if  (f != null){
					if (DictFeature.class.isInstance(f))
						this.sentenceGenerators.add((DictFeature) f);
					else if(Agr1Feature.class.isInstance(f))
						this.agr1Feat = (Agr1Feature)f;
					else
						this.tokenGenerators.add((TokenFeature) f);
					this.attributeIndex.addAttribute(f.getName());
				}
			}
		}
		catch(Exception ex){
            ex.printStackTrace();
            System.out.println(">> " + ex.getMessage());
		}
	}
	
	/**
	 * Return index of token attributes (mapping from feature name to their corresponding
	 * position in the array of attributes).
	 * @return
	 */
	public TokenAttributeIndex getAttributeIndex(){
		return this.attributeIndex;
	}
	
	/**
	 * Generates feature for every token in the paragraph set. The features are added to the 
	 * token list of attributes.
	 * @param ps
	 * @throws Exception
	 */
	public void generateFeatures(ParagraphSet ps) throws Exception {
		ps.getAttributeIndex().update(this.attributeIndex.allAtributes());
		for (Paragraph p : ps.getParagraphs()){
			generateFeatures(p, false);
		}
		ps.getAttributeIndex().update(featureNames);
	}

	public void generateFeatures(Paragraph p, boolean updateAttributeIndex) throws Exception {
		if(updateAttributeIndex)
			p.getAttributeIndex().update(this.attributeIndex.allAtributes());
		for (Sentence s : p.getSentences())
			generateFeatures(s);
		if(updateAttributeIndex)
			p.getAttributeIndex().update(featureNames);
	}

	public void generateFeatures(Sentence s) throws Exception {

		for (Token t : s.getTokens()){
			t.packAtributes(this.attributeIndex.getLength());
			ArrayList<Integer> toDel = new ArrayList<Integer>();
			generateFeatures(t);
			for(String sourceFeat: sourceFeatures)
				if(!featureNames.contains(sourceFeat))
					toDel.add(this.attributeIndex.getIndex(sourceFeat)-toDel.size());
			for(int idx: toDel)
				t.removeAttribute(idx);
		}
		for (DictFeature f : this.sentenceGenerators)
			f.generate(s, this.attributeIndex.getIndex(f.getName()));
		if (agr1Feat != null)
			agr1Feat.generate(s, this.attributeIndex.getIndex("agr1"),
								 this.attributeIndex.getIndex("case"),
								 this.attributeIndex.getIndex("number"),
								 this.attributeIndex.getIndex("gender"));
	}

	public void generateFeatures(Token t) throws Exception {
		for (TokenFeature f : this.tokenGenerators){
			t.setAttributeValue(this.attributeIndex.getIndex(f.getName()), f.generate(t));
		}
	}
}