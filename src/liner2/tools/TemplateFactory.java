package liner2.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import liner2.Main;
import liner2.structure.AttributeIndex;

public class TemplateFactory {
	
	private static final TemplateFactory templateFactory = new TemplateFactory();
	
	private Hashtable<String, Template> templates = new Hashtable<String, Template>();
	
	public static TemplateFactory get() {
		return templateFactory;
	}
	
	public Template getTemplate(String templateName) {
		return this.templates.get(templateName);
	}
	
	public Hashtable<String, Template> getTemplates() {
		return this.templates;
	}
	
	public Set getTemplateNames() {
		return this.templates.keySet();
	}
	
	public void parse(String description) throws Exception {
		Main.log("TemplateFactory.parse("+description+")");
		int pos = description.indexOf(":");
		if (pos == -1)
			throw new Exception("Invalid template description: "+description);
		String templateName = description.substring(0, pos);
		String featureDesc = description.substring(pos+1);
		
		if (this.templates.containsKey(templateName))
			this.templates.get(templateName).addFeature(featureDesc);
		else {
			Template template = new Template();
			template.addFeature(featureDesc);
			this.templates.put(templateName, template);
		}
	}
	
	public void store(String templateName, String filename, AttributeIndex attributeIndex) throws Exception {
		if (!this.templates.containsKey(templateName))
			throw new Exception("Template not found: " + templateName);
		
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new File(filename));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		pw.write("# Unigram\n");
		Template template = this.templates.get(templateName);
		Hashtable<String, String[]> features = template.getFeatures();
		for (String featureName : template.getFeatureNames()) {
			pw.write("# " + featureName + "\n");
			String[] windowDesc = features.get(featureName);
			// cecha pojedyncza
			if (featureName.equals(windowDesc[0])) {
				String featureId = Integer.toString(attributeIndex.getIndex(featureName));
				String featureIdFixed = featureId;
				while (featureIdFixed.length() < 2)
					featureIdFixed = "0" + featureIdFixed;
				for (int i = 1; i < windowDesc.length; i++) {
					String wFixed = windowDesc[i];
					if (!wFixed.startsWith("-"))
						wFixed = "+" + wFixed;
					pw.write("U" + featureIdFixed + wFixed 
						+ ":%x[" + windowDesc[i] + "," + featureId + "]\n");
				}
			}
			// cecha złożona
			else {
				String unigramId = "U";
				String unigramContent = "";
				for (int i = 0; i < windowDesc.length - 1; i += 2) {
					if (unigramId.length() > 1) unigramId += "/";
					if (unigramContent.length() > 0) unigramContent += "/";
					String featureId = Integer.toString(attributeIndex.getIndex(windowDesc[i]));
					String featureIdFixed = featureId;
					while (featureIdFixed.length() < 2)
						featureIdFixed = "0" + featureIdFixed;
					String wFixed = windowDesc[i+1];
					if (!wFixed.startsWith("-"))
						wFixed = "+" + wFixed;
					unigramId += featureIdFixed + wFixed;
					unigramContent += "%x[" + windowDesc[i+1] + "," + featureId + "]";
				}
				pw.write(unigramId + ":" + unigramContent + "\n");
			}
			pw.write("\n");
		}
		
		pw.write("# Bigram\n");
		pw.write("B\n");
		pw.close();
	}
}
