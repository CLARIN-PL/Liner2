package liner2.tools;

import java.util.ArrayList;
import java.util.Hashtable;

import liner2.Main;

public class Template {
	ArrayList<String> featureNames = new ArrayList<String>();
	Hashtable<String, String[]> features = new Hashtable<String, String[]>();
	
	public void addFeature(String description) throws Exception {
		int pos = description.indexOf(":");
		if (pos == -1)
			throw new Exception("Invalid template description: "+description);
		String featureName = description.substring(0, pos);
		String[] windowDesc = description.substring(pos+1).split(":");
		if (this.features.containsKey(featureName))
			throw new Exception("Duplicate feature definition in template description: "+description);
		else {
			this.featureNames.add(featureName);
			this.features.put(featureName, windowDesc);
		}
		
		Main.log("Added feature "+featureName+" with description "+windowDesc);
	}
	
	public ArrayList<String> getFeatureNames() {
		return this.featureNames;
	}
	
	public Hashtable<String, String[]> getFeatures() {
		return this.features;
	}
}
