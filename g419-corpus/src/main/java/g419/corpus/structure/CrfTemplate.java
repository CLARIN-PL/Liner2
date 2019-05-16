package g419.corpus.structure;


import g419.corpus.ConsolePrinter;
import g419.corpus.io.DataFormatException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class CrfTemplate {
  ArrayList<String> usedFeatures = new ArrayList<>();
  ArrayList<String> featureNames = new ArrayList<>();
  Hashtable<String, String[]> features = new Hashtable<>();
  TokenAttributeIndex attributeIndex;

  boolean appendFeatureNameToValue = true;

  public void addFeature(final String description) throws Exception {
    ConsolePrinter.log("(TemplateFactory) Adding feature:" + description);
    final String[] featureUnits = description.split("/");
    if (featureUnits.length < 1) {
      throw new Exception("Invalid template description: " + description);
    }
    // cecha "pojedyncza": featureNames <= nazwa, features <= nazwa, opis okna
    else if (featureUnits.length == 1) {
      final int pos = featureUnits[0].indexOf(":");
      if (pos == -1) {
        throw new Exception("Invalid template description: " + description);
      }
      final String featureName = featureUnits[0].substring(0, pos);
      final String[] windowDesc = featureUnits[0].split(":");
      if (features.containsKey(featureName)) {
        throw new Exception("Duplicate feature definition in template description: " + description);
      } else {
        featureNames.add(featureName);
        if (!usedFeatures.contains(featureName)) {
          usedFeatures.add(featureName);
        }
        features.put(featureName, windowDesc);
      }
    } else if (description.startsWith("/")) {
      final String[] featureUnit = featureUnits[1].split(":");
      if (featureUnit.length != 2) {
        throw new Exception("Invalid template description: " + description);
      }
      final String featureName = featureUnit[0] + "[" + featureUnit[1] + "]";
      final String[] windowDesc = {featureUnit[0], featureUnit[1]};
      if (features.containsKey(featureName)) {
        throw new Exception("Duplicate feature definition in template description: " + description);
      } else {
        if (!usedFeatures.contains(featureUnit[0])) {
          usedFeatures.add(featureUnit[0]);
        }
        featureNames.add(featureName);
        features.put(featureName, windowDesc);
      }
    }
    // cecha złożona:
    // featureNames <= pełna nazwa cechy
    // features <= pary: nazwa_pojedynczej_cechy, pozycja
    else {
      final StringBuilder featureNameB = new StringBuilder("");
      final String[] windowDesc = new String[featureUnits.length * 2];
      for (int i = 0; i < featureUnits.length; i++) {
        final String[] featureUnit = featureUnits[i].split(":");
        if (featureUnit.length != 2) {
          throw new Exception("Invalid template description: " + description);
        }
        if (featureNameB.length() > 0) {
          featureNameB.append("/");
        }
        featureNameB.append(featureUnit[0] + "[" + featureUnit[1] + "]");
        if (!usedFeatures.contains(featureUnit[0])) {
          usedFeatures.add(featureUnit[0]);
        }
        windowDesc[i * 2] = featureUnit[0];
        windowDesc[i * 2 + 1] = featureUnit[1];
      }
      final String featureName = featureNameB.toString();
      if (features.containsKey(featureName)) {
        throw new Exception("Duplicate feature definition in template description: " + description);
      } else {
        featureNames.add(featureName);
        features.put(featureName, windowDesc);
      }
    }
  }

  public ArrayList<String> getFeatureNames() {
    return featureNames;
  }

  public ArrayList<String> getUsedFeatures() {
    return usedFeatures;
  }

  public Hashtable<String, String[]> getFeatures() {
    return features;
  }

  public TokenAttributeIndex expandAttributeIndex(final TokenAttributeIndex attributeIndex) {

    final TokenAttributeIndex result = new TokenAttributeIndex();
    // rozwija cechy: np. base:-1:0:1 -> base-1, base+0, base+1

    try {
      for (final String featureName : featureNames) { // cechy złożone
        if (featureName.indexOf('/') > -1) {
          for (final String atomicFeature : featureName.split("/")) {
            if (attributeIndex.allAtributes().contains(atomicFeature.split("\\[")[0])) {
              result.addAttribute(featureName.replace('/', '_'));
            } else {
              throw new DataFormatException("Error while parsing template: " + atomicFeature + " not specified in data features");
            }
          }
        } else { // cechy proste
          if (attributeIndex.allAtributes().contains(featureName)) {
            final String[] windowDesc = features.get(featureName);

            for (int j = 1; j < windowDesc.length; j++) {
              String w = windowDesc[j];
              if (!w.startsWith("-")) {
                w = "+" + w;
              }
              result.addAttribute(featureName + w);
            }
          } else {
            throw new DataFormatException("Error while parsing template: " + featureName + " not specified in data features");
          }
        }
      }
    } catch (final DataFormatException e) {
      System.out.println(e);
    }
    return result;
  }

  public Sentence expandAttributes(final Sentence sentence) {

    final Sentence newSentence = new Sentence();
    final TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();
    final TokenAttributeIndex newAttributeIndex = expandAttributeIndex(attributeIndex);
    newSentence.setAttributeIndex(newAttributeIndex);
    for (final Annotation chunk : sentence.getChunks()) {
      newSentence.addChunk(chunk);
    }

    final List<Token> tokens = sentence.getTokens();
    for (int k = 0; k < tokens.size(); k++) {
      final Token newToken = new Token(newAttributeIndex);
      for (final Tag tag : tokens.get(k).getTags()) {
        newToken.addTag(tag);
      }
      newToken.clearAttributes();

      for (final String featureName : featureNames) {
        if (featureName.indexOf('/') > -1) { // cechy złożone
          final String newFeatureName = featureName.replace('/', '_');
          final String[] windowDesc = features.get(featureName);

          String featureValue = "";
          for (int i = 0; i < windowDesc.length - 1; i += 2) {
            final int attrIdx = attributeIndex.getIndex(windowDesc[i]);
            final int idx = Integer.parseInt(windowDesc[i + 1]);
            if ((k + idx >= 0) && (k + idx < tokens.size())) {
              if (featureValue.length() > 0) {
                featureValue += "#";
              }
              if (appendFeatureNameToValue) {
                featureValue += featureName + "::" + idx + "::";
              }
              featureValue += tokens.get(k + idx).getAttributeValue(attrIdx);
            }
          }
          final int newAttrIdx = newAttributeIndex.getIndex(newFeatureName);
          newToken.setAttributeValue(newAttrIdx, featureValue);
        } else { // cechy proste
          final String[] windowDesc = features.get(featureName);

          for (int j = 1; j < windowDesc.length; j++) {
            String w = windowDesc[j];
            final int idx = Integer.parseInt(w);
            if (!w.startsWith("-")) {
              w = "+" + w;
            }
            final String newFeatureName = featureName + w;

            String featureValue = null;
            if ((k + idx >= 0) && (k + idx < tokens.size())) {
              featureValue = tokens.get(k + idx).getAttributeValue(attributeIndex.getIndex(featureName));
            }
            final int newAttrIdx = newAttributeIndex.getIndex(newFeatureName);
            if (appendFeatureNameToValue) {
              featureValue = featureName + "::" + idx + "::" + featureValue;
            }
            newToken.setAttributeValue(newAttrIdx, featureValue);
          }
        }
      }

      newSentence.addToken(newToken);
    }

    return newSentence;
  }

  public TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  public void setAttributeIndex(final TokenAttributeIndex attributeIndex) {
    this.attributeIndex = attributeIndex;
  }

  public String printFeatures() {
    final StringBuilder sb = new StringBuilder();
    for (final String featureName : featureNames) {
      if (featureName.indexOf('/') > -1) {
        sb.append(featureName.replace('/', '_') + "\n");
      } else {
        final String[] windowDesc = features.get(featureName);
        for (int j = 1; j < windowDesc.length; j++) {
          sb.append(String.format("%s[%s]\n", featureName, windowDesc[j]));
        }
      }
    }
    return sb.toString();
  }
}
