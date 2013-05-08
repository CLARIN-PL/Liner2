package liner2.features.tokens;

import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;

public class TokenFeatureFactory {

	/**
	 * Create a token feature generator from string description.
	 * @param feature
	 * @return
	 */
	static WordnetLoader database = null;
	private static List sourceFeats = Arrays.asList("orth", "base", "ctag");
	
	public static Feature create(String feature) throws Exception{
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
		else if (feature.equals("starts_with_upper_case")) 
			return new StartsWithUpperFeature(feature);
		else if (feature.equals("starts_with_lower_case")) 
			return new StartsWithLowerFeature(feature);
		else if (feature.equals("starts_with_digit")) 
			return new StartsWithDigitFeature(feature);
		else if (feature.equals("starts_with_symbol")) 
			return new StartsWithSymbolFeature(feature);
		else if (feature.equals("has_upper_case")) 
            return new HasUpperFeature(feature);
        else if (feature.equals("has_lower_case")) 
            return new HasLowerFeature(feature);
        else if (feature.equals("has_digit")) 
            return new HasDigitFeature(feature);
        else if (feature.equals("has_symbol")) 
            return new HasSymbolFeature(feature);
        else if (feature.equals("arg1"))
        	return new Arg1Feature(feature);
        else if (feature.endsWith(".txt")){
    		String[] fData = feature.split(":");
    		if(fData.length != 3)
    			throw new DataFormatException("Invalid feature description: "+feature);
        	int sourceFeatureIndex;
			if(fData[1].equals("orth"))
        		sourceFeatureIndex = 0;
        	else if(fData[1].equals("base"))
        		sourceFeatureIndex = 1;
        	else // "ctag"
        		sourceFeatureIndex = 2;
        	return new DictFeature(fData[0], fData[2], sourceFeatureIndex);
        }
        else if (feature.startsWith("synonym")){
        	String[] fData = feature.split(":");
        	if(fData.length != 2)
    			throw new DataFormatException("Invalid feaeture description: "+feature);
        	if(database == null)
        		database = new WordnetLoader(fData[1]);
        	return new SynonymFeature(fData[0], database);
        }
        else if (feature.startsWith("hypernym")){
        	String[] fData = feature.split(":");
        	if(fData.length != 3)
    			throw new DataFormatException("Invalid feaeture description: "+feature);
        	if(database == null)
        		database = new WordnetLoader(fData[2]);
        	return new HypernymFeature(fData[0]+fData[1], database, Integer.parseInt(fData[1]));
        }
        else if (sourceFeats.contains(feature))  //zwroci null dla orth, base i ctag bo sa pobierane z pliku zrodlowego wiec nie potrzebe sa dla nich generatory
        	return null;
        else 
			throw new DataFormatException("Invalid feature: "+feature);
	}
	
}
