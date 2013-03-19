package liner2.writer;

import java.util.ArrayList;
import java.util.Hashtable;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

import liner2.structure.TokenAttributeIndex;
import liner2.structure.Annotation;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

import liner2.tools.Template;
import liner2.tools.TemplateFactory;

public class ArffStreamWriter extends StreamWriter {

	private BufferedWriter ow;
	private boolean init = false;

	public ArffStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	protected void init(TokenAttributeIndex attributeIndex) {
		if (this.init)
			return;
		try {
//			String line = "-DOCSTART CONFIG FEATURES orth base ctag";
			TokenAttributeIndex newAttributeIndex = expandAttributeIndex("t1", attributeIndex);
			String line = "@relation rel";
			ow.write(line, 0, line.length());
			ow.newLine();
			ow.newLine();
			for (int i = 0; i < newAttributeIndex.getLength(); i++) {
				String featureName = newAttributeIndex.getName(i);
				line = "@attribute " + newAttributeIndex.getName(i) + " string";
				ow.write(line, 0, line.length());
				ow.newLine();
			}
			line = "@attribute iobtag string";
			ow.write(line, 0, line.length());
			ow.newLine();
			ow.newLine();
			line = "@data";
			ow.write(line, 0, line.length());
			ow.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.init = true;
	}

	@Override
	public void close() {
		try {
			ow.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!init)
				init(paragraph.getAttributeIndex());
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(expandAttributes("t1", sentence));
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeSentence(Sentence sentence) throws IOException {
		ArrayList<Token> tokens = sentence.getTokens();
		for (int i = 0; i < tokens.size(); i++) {
			writeToken(i, tokens.get(i), sentence);
		}
	}
	
	private void writeToken(int idx, Token token, Sentence sentence) 
		throws IOException {
//		String line = token.getFirstValue();
//		ArrayList<Tag> tags = token.getTags();
//		Tag firstTag = tags.get(0);
//		line += " " + firstTag.getBase() + " " + firstTag.getCtag();
		String line = "";
//		for (int i = 0; i < token.getNumAttributes(); i++)
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++) {
//			System.out.println("" + i + ": " + sentence.getAttributeIndex().getName(i));
			String attrval = token.getAttributeValue(i);
			if (attrval == null)
				attrval = "?";
			else
				attrval = "\'" + attrval.replace("\'", "\\\'") + "\'";
			line += (line.length() > 0 ? ",\t" : "") + attrval;
		}
		
		Annotation chunk = sentence.getChunkAt(idx);
		if (chunk == null)
			line += ",\tO";
		else {
			if (idx == chunk.getBegin())
				line += ",\tB-" + chunk.getType();
			else
				line += ",\tI-" + chunk.getType();
		}
		ow.write(line, 0, line.length());
		ow.newLine();
	}
		
	
	private TokenAttributeIndex expandAttributeIndex(String templateName, TokenAttributeIndex attributeIndex) 
		throws Exception {
		TokenAttributeIndex result = new TokenAttributeIndex();
		// rozwija cechy: np. base:-1:0:1 -> base-1, base+0, base+1
		Template template = TemplateFactory.get().getTemplate(templateName);
		ArrayList<String> featureNames = template.getFeatureNames();
		Hashtable<String, String[]> features = template.getFeatures();
		for (int i = 0; i < attributeIndex.getLength(); i++) {
			String featureName = attributeIndex.getName(i);
			if (featureNames.contains(featureName)) {
				String[] windowDesc = features.get(featureName);
				
				for (int j = 1; j < windowDesc.length; j++) {
					String w = windowDesc[j];
					if (!w.startsWith("-")) w = "+" + w;
					result.addAttribute(featureName + w);
				}
			}
			else {
				throw new Exception("Feature not found: " + featureName);
			}
		}
		// cechy złożone
		for (String featureName : template.getFeatureNames()) {
			if (featureName.indexOf('/') > -1) {
				result.addAttribute(featureName.replace('/', '_'));
			}
		}
		return result;
	}
	
	public Sentence expandAttributes(String templateName, Sentence sentence) throws Exception {
		
		Sentence newSentence = new Sentence();
		TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();
		TokenAttributeIndex newAttributeIndex = expandAttributeIndex(templateName, attributeIndex);
		newSentence.setAttributeIndex(newAttributeIndex);
		for (Annotation chunk : sentence.getChunks())
			newSentence.addChunk(chunk);
		
		Template template = TemplateFactory.get().getTemplate(templateName);
		ArrayList<String> featureNames = template.getFeatureNames();
		Hashtable<String, String[]> features = template.getFeatures();
		
		ArrayList<Token> tokens = sentence.getTokens();
		for (int k = 0; k < tokens.size(); k++) {
			Token newToken = new Token();
			for (Tag tag : tokens.get(k).getTags())
				newToken.addTag(tag);
			newToken.clearAttributes();
			
			// cechy proste
			for (int i = 0; i < attributeIndex.getLength(); i++) {
				String featureName = attributeIndex.getName(i);
				if (featureNames.contains(featureName)) {
					String[] windowDesc = features.get(featureName);
					
					for (int j = 1; j < windowDesc.length; j++) {
						String w = windowDesc[j];
						int idx = Integer.parseInt(w);
						if (!w.startsWith("-")) w = "+" + w;
						String newFeatureName = featureName + w;
//						newAttributeIndex.addAttribute(newFeatureName);
					
						String featureValue = null;
						if ((k + idx >= 0) && (k + idx < tokens.size()))
							featureValue = tokens.get(k+idx).getAttributeValue(i);
						int newAttrIdx = newAttributeIndex.getIndex(newFeatureName);
						newToken.setAttributeValue(newAttrIdx, featureValue);
					}
				} else {
					throw new Exception("Feature not found: " + featureName);
				}
			}

			// cechy złożone
			for (String featureName : template.getFeatureNames()) {	
				if (featureName.indexOf('/') > -1) {
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
			}
			
			newSentence.addToken(newToken);
		}
				
		return newSentence;
	}
}
