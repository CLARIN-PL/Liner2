package liner2.tools;

import java.io.*;

import java.util.*;

import liner2.LinerOptions;
import liner2.Main;
import liner2.structure.TokenAttributeIndex;

public class TemplateFactory {

    public static void parseFeature(String description, HashMap<String, Template> templates, Set<String> validFeatures) throws Exception{

        Main.log("TemplateFactory.parseFeature("+description+")");
        int pos = description.indexOf(":");
        if (pos == -1){
            throw new Exception("Invalid template description: "+description);
        }

        String templateName = description.substring(0, pos);
        String featureDesc = description.substring(pos+1);
        if (templates.containsKey(templateName)) {
            templates.get(templateName).addFeature(featureDesc);
        }
        else {
            Template template = new Template(validFeatures);
             template.addFeature(featureDesc);
             templates.put(templateName, template);
        }

    }

	
	public static void store(Template template, String filename, TokenAttributeIndex attributeIndex) throws Exception {
		
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new File(filename));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		pw.write("# Unigram\n");
		Hashtable<String, String[]> features = template.getFeatures();
		for (String featureName : template.getFeatureNames()) {
			pw.write("# " + featureName + "\n");
			String[] windowDesc = features.get(featureName);
			// cecha pojedyncza
			if (featureName.equals(windowDesc[0])) {
				String featureId = Integer.toString(attributeIndex.getIndex(featureName));
                if(featureId.equals("-1")){
                	pw.close();
                    throw new Exception("Feature not found: "+featureName);
                }
				String featureIdFixed = featureId;
				while (featureIdFixed.length() < 2)
					featureIdFixed = "0" + featureIdFixed;
				for (int i = 1; i < windowDesc.length; i++) {
					String wFixed = windowDesc[i];
					if (!wFixed.startsWith("-"))
						wFixed = "+" + wFixed;
					pw.write("U" + featureIdFixed + wFixed + ":%x[" + windowDesc[i] + "," + featureId + "]\n");
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
