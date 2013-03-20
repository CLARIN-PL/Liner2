package liner2.features;

import java.util.ArrayList;

import liner2.features.tokens.ATokenFeature;
import liner2.features.tokens.TokenFeatureFactory;
import liner2.structure.ParagraphSet;
import liner2.structure.TokenAttributeIndex;

public class TokenFeatureGenerator {

	private ArrayList<ATokenFeature> generators = new ArrayList<ATokenFeature>();
	private TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	
	/**
	 * 
	 * @param features — array with feature definitions
	 */
	public TokenFeatureGenerator(ArrayList<String> features){
		for ( String feature : features ){
			ATokenFeature f = TokenFeatureFactory.create(feature);
			this.generators.add(f);
			this.attributeIndex.addAttribute(f.getName());
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
		
	}
	
}
