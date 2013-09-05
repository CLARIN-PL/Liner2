package liner2.tools;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import liner2.LinerOptions;

public class Template {
	ArrayList<String> featureNames = new ArrayList<String>();
	Hashtable<String, String[]> features = new Hashtable<String, String[]>();
    Set<String> validFeatures;

    public Template(Set<String> validFeatures){
        this.validFeatures = validFeatures;
    }
	
	public void addFeature(String description) throws Exception {
		String[] featureUnits = description.split("/");
		if (featureUnits.length < 1)
			throw new Exception("Invalid template description: " + description);
		// cecha "pojedyncza": featureNames <= nazwa, features <= nazwa, opis okna
		else if (featureUnits.length == 1) {
			int pos = featureUnits[0].indexOf(":");
			if (pos == -1)
				throw new Exception("Invalid template description: " + description);
			String featureName = featureUnits[0].substring(0, pos);
			if(!validFeatures.contains(featureName))
				throw new DataFormatException("Error while parsing template: "+featureName+" not specified in features");

			String[] windowDesc = featureUnits[0].split(":");
			if (this.features.containsKey(featureName))
				throw new Exception("Duplicate feature definition in template description: "+description);
			else {
				this.featureNames.add(featureName);
				this.features.put(featureName, windowDesc);
			}
		}
		// cecha złożona:
		// featureNames <= pełna nazwa cechy
		// features <= pary: nazwa_pojedynczej_cechy, pozycja
		else {
			StringBuilder featureNameB = new StringBuilder("");
			String[] windowDesc = new String[featureUnits.length * 2];
			for (int i = 0; i < featureUnits.length; i++) {
				String[] featureUnit = featureUnits[i].split(":");
				if (featureUnit.length != 2)
					throw new Exception("Invalid template description: " + description);
                if (!validFeatures.contains(featureUnit[0]))
                    throw new DataFormatException("Error while parsing template: "+featureUnit[0]+" not specified in features");
				if (featureNameB.length() > 0)
					featureNameB.append("/");
				featureNameB.append(featureUnit[0] + "[" + featureUnit[1] + "]");
				windowDesc[i*2] = featureUnit[0];
				windowDesc[i*2+1] = featureUnit[1];
			}
			String featureName = featureNameB.toString();
			if (this.features.containsKey(featureName))
				throw new Exception("Duplicate feature definition in template description: "+description);
			else {
				this.featureNames.add(featureName);
				this.features.put(featureName, windowDesc);
			}
		}
	}
	
	public ArrayList<String> getFeatureNames() {
		return this.featureNames;
	}
	
	public Hashtable<String, String[]> getFeatures() {
		return this.features;
	}
}
