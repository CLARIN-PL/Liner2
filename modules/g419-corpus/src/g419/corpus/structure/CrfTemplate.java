package g419.corpus.structure;


import g419.corpus.Logger;
import g419.corpus.io.DataFormatException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;


public class CrfTemplate {
    TreeSet<String> usedFeatures = new TreeSet<>();
	ArrayList<String> featureNames = new ArrayList<String>();
	Hashtable<String, String[]> features = new Hashtable<String, String[]>();
    TokenAttributeIndex attributeIndex;
	
	public void addFeature(String description) throws Exception {
        Logger.log("(TemplateFactory) Adding feature:" + description);
		String[] featureUnits = description.split("/");
		if (featureUnits.length < 1)
			throw new Exception("Invalid template description: " + description);
		// cecha "pojedyncza": featureNames <= nazwa, features <= nazwa, opis okna
		else if (featureUnits.length == 1) {
			int pos = featureUnits[0].indexOf(":");
			if (pos == -1)
				throw new Exception("Invalid template description: " + description);
			String featureName = featureUnits[0].substring(0, pos);
			String[] windowDesc = featureUnits[0].split(":");
			if (this.features.containsKey(featureName))
				throw new Exception("Duplicate feature definition in template description: "+description);
			else {
				this.featureNames.add(featureName);
                this.usedFeatures.add(featureName);
				this.features.put(featureName, windowDesc);
			}
		}
		else if (description.startsWith("/")){
			String[] featureUnit = featureUnits[1].split(":");
			if (featureUnit.length != 2)
				throw new Exception("Invalid template description: " + description);
			String featureName = featureUnit[0] + "[" + featureUnit[1] + "]";
			String[] windowDesc = {featureUnit[0], featureUnit[1]};
			if (this.features.containsKey(featureName))
				throw new Exception("Duplicate feature definition in template description: "+description);
			else {
                this.usedFeatures.add(featureUnit[0]);
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
				if (featureNameB.length() > 0)
					featureNameB.append("/");
				featureNameB.append(featureUnit[0] + "[" + featureUnit[1] + "]");
                usedFeatures.add(featureUnit[0]);
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

    public TreeSet<String> getUsedFeatures() {
        return this.usedFeatures;
    }
	
	public Hashtable<String, String[]> getFeatures() {
		return this.features;
	}

    public TokenAttributeIndex expandAttributeIndex(TokenAttributeIndex attributeIndex){

        TokenAttributeIndex result = new TokenAttributeIndex();
        // rozwija cechy: np. base:-1:0:1 -> base-1, base+0, base+1

        try {
            for (String featureName : featureNames) { // cechy złożone
                if (featureName.indexOf('/') > -1) {
                    for(String atomicFeature: featureName.split("/")){
                        if (attributeIndex.allAtributes().contains(atomicFeature.split("\\[")[0])) {
                            result.addAttribute(featureName.replace('/', '_'));
                        } else {
                            throw new DataFormatException("Error while parsing template: " + atomicFeature + " not specified in data features");
                        }
                    }
                }
                else{ // cechy proste
                    if (attributeIndex.allAtributes().contains(featureName)) {
                        String[] windowDesc = features.get(featureName);

                        for (int j = 1; j < windowDesc.length; j++) {
                            String w = windowDesc[j];
                            if (!w.startsWith("-")) w = "+" + w;
                            result.addAttribute(featureName + w);
                        }
                    }
                    else {
                        throw new DataFormatException("Error while parsing template: " + featureName + " not specified in data features");
                    }
                }
            }
        }
        catch(DataFormatException e){
            System.out.println(e);
            }
        return result;
    }

    public Sentence expandAttributes(Sentence sentence) {

        Sentence newSentence = new Sentence();
        TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();
        TokenAttributeIndex newAttributeIndex = expandAttributeIndex(attributeIndex);
        newSentence.setAttributeIndex(newAttributeIndex);
        for (Annotation chunk : sentence.getChunks())
            newSentence.addChunk(chunk);

        ArrayList<Token> tokens = sentence.getTokens();
        for (int k = 0; k < tokens.size(); k++) {
            Token newToken = new Token(newAttributeIndex);
            for (Tag tag : tokens.get(k).getTags())
                newToken.addTag(tag);
            newToken.clearAttributes();

            for (String featureName : featureNames) {
                if (featureName.indexOf('/') > -1) { // cechy złożone
                    String newFeatureName = featureName.replace('/', '_');
                    String[] windowDesc = features.get(featureName);

                    String featureValue = "";
                    for (int i = 0; i < windowDesc.length-1; i += 2) {
                        int attrIdx = attributeIndex.getIndex(windowDesc[i]);
                        int idx = Integer.parseInt(windowDesc[i+1]);
                        if ((k + idx >= 0) && (k + idx < tokens.size())) {
                            if (featureValue.length() > 0) featureValue += "#";
                            featureValue += tokens.get(k+idx).getAttributeValue(attrIdx);
                        }
                    }
                    int newAttrIdx = newAttributeIndex.getIndex(newFeatureName);
                    newToken.setAttributeValue(newAttrIdx, featureValue);
                }
                else{ // cechy proste
                    String[] windowDesc = features.get(featureName);

                    for (int j = 1; j < windowDesc.length; j++) {
                        String w = windowDesc[j];
                        int idx = Integer.parseInt(w);
                        if (!w.startsWith("-")) w = "+" + w;
                        String newFeatureName = featureName + w;

                        String featureValue = null;
                        if ((k + idx >= 0) && (k + idx < tokens.size()))
                            featureValue = tokens.get(k+idx).getAttributeValue(attributeIndex.getIndex(featureName));
                        int newAttrIdx = newAttributeIndex.getIndex(newFeatureName);
                        newToken.setAttributeValue(newAttrIdx, featureValue);
                    }
                }
            }

            newSentence.addToken(newToken);
        }

        return newSentence;
    }

    public TokenAttributeIndex getAttributeIndex(){
        return attributeIndex;
    }

    public void setAttributeIndex(TokenAttributeIndex attributeIndex){
        this.attributeIndex = attributeIndex;
    }

    public String printFeatures(){
        StringBuilder sb = new StringBuilder();
        for (String featureName : featureNames){
            if (featureName.indexOf('/') > -1) {
                sb.append(featureName.replace('/', '_') + "\n");
            }
            else{
                String[] windowDesc = features.get(featureName);
                for (int j = 1; j < windowDesc.length; j++) {
                    sb.append(String.format("%s[%s]\n",featureName, windowDesc[j]));
                }
            }
        }
        return  sb.toString();
    }
}
