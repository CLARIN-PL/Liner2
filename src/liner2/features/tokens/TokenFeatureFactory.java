package liner2.features.tokens;

public class TokenFeatureFactory {

	/**
	 * Create a token feature generator from string description.
	 * @param feature
	 * @return
	 */
	public static ATokenFeature create(String feature){
		if (feature.equals("class"))
			return new ClassFeature(feature);
		else if (feature.equals("case")) 
			return new CaseFeature(feature);
		else if (feature.equals("number"))
			return new NumberFeature(feature);
		else if (feature.equals("gender"))
			return new GenderFeature(feature);
		else if (feature.equals("pattern"))
			return new PatternFeature(feature);
		else if (feature.startsWith("prefix"))
			return new PrefixFeature(feature);
		else if (feature.startsWith("suffix"))
			return new SuffixFeature(feature);
		else
			return null;
	}
	
}
