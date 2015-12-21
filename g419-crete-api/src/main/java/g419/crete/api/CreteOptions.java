package g419.crete.api;

import g419.crete.api.annotation.AnnotationDescription;
import g419.crete.api.annotation.AnnotationSelectorFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class CreteOptions {

	private static class CreteOptionsHolder {
        private static final CreteOptions OPTIONS = new CreteOptions();
    }
	
	public static CreteOptions getOptions(){
		return CreteOptionsHolder.OPTIONS;
	}
	
	private CreteOptions(){
		properties = new Properties();
	}
	
	protected Properties properties;
	public Properties getProperties(){return properties;}
	
	protected HashMap<String, LinkedHashMap<String, String>> features;
	public HashMap<String, LinkedHashMap<String, String>> getFeatures(){return features;}
	
	protected HashMap<String, AnnotationDescription> annotations;
	public HashMap<String, AnnotationDescription> getAnnotations(){return annotations;}
	
	protected HashMap<String, List<AnnotationDescription>> selectors;
	public HashMap<String, List<AnnotationDescription>> getSelectors(){ return selectors;}

	protected HashMap<String, String> classifierParameters;
	public HashMap<String, String> getClassifierParameters(){ return classifierParameters;}

	
	public static final String POSTFIX_FEATURES = "_features";
	public static final String PREFIX_ANNOTATION_TYPE = "annotation_type_";
	public static final String PREFIX_SELECTOR = "selector_";
	
	private final String FEATURE_SEPARATOR = ",";
	private final String FEATURE_NEGATION = "!";
	private final String FEATURE_ARRAY_START = "[";
	private final String FEATURE_ARRAY_END = "]";
	
	public static final String OPTION_ANNOTATIONS = "annotations";
	public static final String OPTION_CLASSIFIERS = "classifiers";
	public static final String OPTION_SELECTORS = "selectors";
	public static final String OPTION_SELECTOR_TYPE = "selector_type";
	
	public static final String SECTION_MAIN = "main";
	public static final String SECTION_INCLUDE = "include";
	public static final String SECTION_FEATURES = "features";
	public static final String SECTION_CLASSIFIER_PARAMETERS = "classifier_parameters";
	
	
	
	public void parseModelIni(String filename){
		try {
            File iniFile = new File(filename);
            filename = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
            Ini ini = new Ini(iniFile);
            Ini.Section main = ini.get(SECTION_MAIN);
            
            for(Entry<String, String> entry : main.entrySet())
            	properties.setProperty(entry.getKey(), entry.getValue().replace("{INI_PATH}", filename));
            
            Ini.Section include = ini.get(SECTION_INCLUDE);
            if (include.containsKey(OPTION_ANNOTATIONS)) this.annotations = parseAnnotations(include.get(OPTION_ANNOTATIONS).replace("{INI_PATH}", filename));
            if (include.containsKey(OPTION_SELECTORS)) this.selectors = parseSelectors(include.get(OPTION_SELECTORS).replace("{INI_PATH}", filename));
            //            if (include.containsKey(OPTION_CLASSIFIERS)) this.classifiers = parseClassifiers(include.get(OPTION_CLASSIFIERS).replace("{INI_PATH}", filename));


            Ini.Section features = ini.get(SECTION_FEATURES);
            this.features = new HashMap<String, LinkedHashMap<String, String>>();
            for(Entry<String, String> entry : features.entrySet())
            	this.features.put(entry.getKey().replace(POSTFIX_FEATURES, ""), parseFeatures(entry.getValue().replace("{INI_PATH}", filename)));

			Ini.Section classifier_parameters = ini.get(SECTION_CLASSIFIER_PARAMETERS);
			if(classifier_parameters != null) {
				this.classifierParameters = new HashMap<>();
				for(Entry<String, String> entry : classifier_parameters.entrySet())
					this.classifierParameters.put(entry.getKey(), entry.getValue());
			}
            
		}
		catch(Exception ex){
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	private HashMap<String, List<AnnotationDescription>> parseSelectors(String selectorsFile) throws IOException{
		HashMap<String, List<AnnotationDescription>> selectors = new HashMap<String, List<AnnotationDescription>>();
		File iniFile = new File(selectorsFile);
        if(!iniFile.exists())     {
            throw new FileNotFoundException("Error while parsing features:"+selectorsFile+" is not an existing file!");
        }
        String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        Ini ini = new Ini(iniFile);
        
        for(Entry<String, Section> sectionEntry : ini.entrySet()){
        	String selectorName = sectionEntry.getKey().replace(PREFIX_SELECTOR, "");
        	String selectorTypeName = sectionEntry.getValue().get(OPTION_SELECTOR_TYPE);
        	List<AnnotationDescription> selectorAnnotations = new ArrayList<AnnotationDescription>();
        	List<String> annotationNames = parseComplexValue(sectionEntry.getValue().get(OPTION_ANNOTATIONS));
        	for(String annotationName : annotationNames)
        		selectorAnnotations.add(annotations.get(annotationName));
        	
        	AnnotationSelectorFactory.getFactory().addMapping(selectorName, selectorTypeName);
        	selectors.put(selectorName, selectorAnnotations);
        }
               
        return selectors;
	}
	
	private HashMap<String, AnnotationDescription> parseAnnotations(String annotationsFile) throws IOException{
		HashMap<String, AnnotationDescription> annotationDescriptions = new HashMap<String, AnnotationDescription>();

		File iniFile = new File(annotationsFile);
        if(!iniFile.exists())     {
            throw new FileNotFoundException("Error while parsing features:"+annotationsFile+" is not an existing file!");
        }
        String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        Ini ini = new Ini(iniFile);
        
        for(Entry<String, Section> sectionEntry : ini.entrySet()){
        	String annotationDescriptionName = sectionEntry.getKey().replace(PREFIX_ANNOTATION_TYPE, "");
        	HashMap<String, List<String>> config = new HashMap<String, List<String>>();
        	for(Entry<String, String> configEntry : sectionEntry.getValue().entrySet())
        		config.put(configEntry.getKey(), parseComplexValue(configEntry.getValue()));
        	AnnotationDescription description = new AnnotationDescription(config);
        	annotationDescriptions.put(annotationDescriptionName, description);
        }
		
		return annotationDescriptions;	
	}
	
	private LinkedHashMap<String, String> parseFeatures(String featuresFile) throws IOException{
		LinkedHashMap<String, String> features = new LinkedHashMap<String, String>(); 
		
		File iniFile = new File(featuresFile);
        if(!iniFile.exists())     {
            throw new FileNotFoundException("Error while parsing features:"+featuresFile+" is not an existing file!");
        }
        String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        
        for(String feature: parseLines(iniFile))
            features.put(feature.trim(), feature.trim());
            
        return features;
	}
	
	private ArrayList<String> parseLines(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuffer sb = new StringBuffer();
        String line = br.readLine();
        while(line != null) {
            if(!line.isEmpty() && !line.startsWith("#")){
                line = line.trim();
                lines.add(line);
            }
            line = br.readLine();
        }
        return lines;
    }
	
	private List<String> parseComplexValue(String valueString){
		List<String> values = new ArrayList<String>();
		if(valueString == null){
			return values;
		}
		if(valueString.startsWith(FEATURE_NEGATION)){
			values.add("!");
			valueString = valueString.substring(1);
		}
		if(valueString.startsWith(FEATURE_ARRAY_START) && valueString.endsWith(FEATURE_ARRAY_END)){
			String[] valueStrings = valueString.substring(1, valueString.length() - 1).split(FEATURE_SEPARATOR);
			for(String value : valueStrings) 
				values.add(value.trim());
		}
		else{
			values.add(valueString);
		}
		
		return values;
	}
}
