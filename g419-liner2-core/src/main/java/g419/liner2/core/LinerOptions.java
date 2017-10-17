package g419.liner2.core;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import g419.liner2.core.tools.CrfppLoader;
import org.ini4j.Ini;

/**
 * This class handles module parameters. The parameters are read from
 * console and from ini files.
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 *
 */
public class LinerOptions {

	/**
	 * There is one LinetOptions object. 
	 */
	static protected LinerOptions linerOptions = new LinerOptions();

    public static void resetGlobal(){
        linerOptions = new LinerOptions();
    }

	/**
	 * Read-only access to the LinerOptions 
	 * @return
	 */
	public static LinerOptions getGlobal(){
		return linerOptions;
	}
	
	/**
	 * Get property value
	 */
	public String getOption(String option) {
		return properties.getProperty(option);
	}

    public String getOptionUse() {
        return properties.getProperty(OPTION_USED_CHUNKER);
    }

	protected Properties properties;

	public static final String OPTION_FEATURES = "features";
	public static final String OPTION_TYPES = "types";
	public static final String OPTION_USED_CHUNKER = "chunker";
    public static final String OPTION_CRFLIB = "crflib";

    public LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();
	public LinkedHashSet<Ini.Section> chunkersDescriptions = new LinkedHashSet<Ini.Section>();
    public List<Pattern> types = new ArrayList<Pattern>();
	 
	/**
	 * Constructor
	 */
	public LinerOptions(){
		this.properties = new Properties();
	}
	
	public static boolean isGlobalOption(String name){
		return LinerOptions.getGlobal().getProperties().containsKey(name);		
	}

	public boolean isOption(String name){
		return LinerOptions.getGlobal().getProperties().containsKey(name);		
	}

	public Properties getProperties() {
		return this.properties;
	}
    
    /**
     * Read configuration from an ini file.
     * @param iniPath
     */
    public void parseModelIni(String iniPath){
        try {
            File iniFile = new File(iniPath);
            iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
            Ini ini = new Ini(iniFile);
            Ini.Section main = ini.get("main");

            if (main.containsKey(OPTION_FEATURES)) {
                this.features = parseFeatures(main.get(OPTION_FEATURES).replace("{INI_PATH}", iniPath));
            }
            if (main.containsKey(OPTION_TYPES)) {
                this.types = parseTypes(main.get(OPTION_TYPES).replace("{INI_PATH}", iniPath));
            }
            if (main.containsKey(OPTION_CRFLIB)) {
                try {
                    String crfppPath = main.get(OPTION_CRFLIB).replace("{INI_PATH}", iniPath);
                    CrfppLoader.load(crfppPath);
                } catch (UnsatisfiedLinkError e) {
                    System.err.println("Cannot load the libCRFPP.so native code.\n" + e);
                    System.exit(1);
                }
            }
            if (main.containsKey(OPTION_USED_CHUNKER)) {
                this.properties.setProperty(OPTION_USED_CHUNKER, main.get(OPTION_USED_CHUNKER));
            }

            Collection<Ini.Section> chunkerSections = ini.values();
            chunkerSections.remove(main);
            for (Ini.Section chunker : chunkerSections) {
                for(String param: chunker.keySet()){
                    chunker.put(param,chunker.get(param).replace("{INI_PATH}", iniPath));
                }
                this.chunkersDescriptions.add(chunker);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public LinkedHashMap<String, String> parseFeatures(String featuresFile) throws IOException {
        LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();
        File iniFile = new File(featuresFile);
        if(!iniFile.exists())     {
            throw new FileNotFoundException("Error while parsing features:"+featuresFile+" is not an existing file!");
        }
        String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        for(String feature: parseLines(iniFile)){
            feature = feature.trim().replace("{INI_PATH}", iniPath);
            String[] splitted = feature.split(":");
            String featureName;
            if(splitted.length > 2 && splitted[1].length() == 1) {
                featureName = splitted[0]+splitted[1];
            }
            else {
                featureName = splitted[0];
            }
            features.put(featureName, feature);
        }
        return features;
    }

    public List<Pattern> parseTypes(String typesFile) throws IOException {
        List<Pattern> types = new ArrayList<Pattern>();
        File iniFile = new File(typesFile);
        if(!iniFile.exists())     {
            throw new FileNotFoundException("Error while parsing types:"+typesFile+" is not an existing file!");
        }
        for(String type: parseLines(iniFile)){
            types.add(Pattern.compile("^"+type+"$"));
        }
        return types;
    }

    public ArrayList<String> parseLines(File file) throws IOException {
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

    public void setCVTrainData(String data){
        properties.setProperty("cvData", data);
    }

    public void setCVDataFormat(String format){
        properties.setProperty("cvFormat", format);
    }
    
    public List<Pattern> getTypes(){
    	return this.types;
    }
}
