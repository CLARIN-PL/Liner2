package g419.liner2.api.tools;


import g419.corpus.ConsolePrinter;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.*;
import java.util.Hashtable;


public class TemplateFactory {

    public static CrfTemplate parseTemplate(String templateFile) throws Exception{

        ConsolePrinter.log("(TemplateFactory) parsing template: " + templateFile);
        CrfTemplate template = new CrfTemplate();
        BufferedReader br = new BufferedReader(new FileReader(templateFile));
        StringBuffer sb = new StringBuffer();
        String feature = br.readLine();
        while(feature != null) {
            if(!feature.isEmpty() && !feature.startsWith("#")){
                feature = feature.trim();
                template.addFeature(feature);
                ConsolePrinter.log("(TemplateFactory) feature:" + feature);
            }
            feature = br.readLine();
        }
        return template;
    }

	
	public static void store(CrfTemplate template, String filename) throws Exception {
		
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
				String featureId = Integer.toString(template.getUsedFeatures().indexOf(featureName));
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
					String featureId = Integer.toString(template.getUsedFeatures().indexOf(windowDesc[i]));
					String featureIdFixed = featureId;
					while (featureIdFixed.length() < 2)
						featureIdFixed = "0" + featureIdFixed;
					String wFixed = windowDesc[i+1];

					if (wFixed.endsWith("B")){
						unigramId = unigramId.replace("U","B");
						wFixed = wFixed.replace("B","");
					}
					if (!wFixed.startsWith("-") && !wFixed.equals("0"))
						wFixed = "+" + wFixed;
					unigramId += featureIdFixed + wFixed;
					unigramContent += "%x[" + windowDesc[i+1].replace("B","") + "," + featureId + "]";
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
