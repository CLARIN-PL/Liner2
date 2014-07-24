package g419.liner2.api;

import g419.corpus.structure.CrfTemplate;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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
	static protected final LinerOptions linerOptions = new LinerOptions();
	
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
    public static final String OPTION_CHUNKER_DESCRIPTION = "description";

	public boolean verbose = false;
	public boolean verboseDetails = false;
    public boolean libCRFPPLoaded = false;

    public LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();
    public HashMap<String, CrfTemplate> templates = new HashMap<String, CrfTemplate>();
	public LinkedHashMap<String, String> chunkersDescriptions = new LinkedHashMap<String, String>();
    public List<Pattern> types = new ArrayList<Pattern>();

    public HashMap<String, LinerOptions> models = null;
    public String defaultModel = null;
	 
	/**
	 * Constructor
	 */
	public LinerOptions(){
		this.properties = new Properties();
	}
	
	public static boolean isOption(String name){
		return LinerOptions.getGlobal().getProperties().containsKey(name);		
	}

	public Properties getProperties() {
		return this.properties;
	}
    
    /**
     * Read configuration from an ini file.
     * @param iniPath
     * @throws Exception 
     */
    public void parseModelIni(String iniPath){
        try {
            File iniFile = new File(iniPath);
            iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
            Ini ini = new Ini(iniFile);
            Ini.Section main = ini.get("main");

            if (main.containsKey(OPTION_FEATURES)) {
                parseFeatures(main.get(OPTION_FEATURES).replace("{INI_PATH}", iniPath));
            }
            if (main.containsKey(OPTION_TYPES)) {
                parseTypes(main.get(OPTION_TYPES).replace("{INI_PATH}", iniPath));
            }
            if (main.containsKey(OPTION_CRFLIB)) {
                try {
                    System.load(main.get(OPTION_CRFLIB).replace("{INI_PATH}", iniPath));
                    libCRFPPLoaded = true;
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
                this.chunkersDescriptions.put(chunker.getName().substring(8), chunker.get(OPTION_CHUNKER_DESCRIPTION).replace("{INI_PATH}", iniPath));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public LinkedHashMap<String, String> parseFeatures(String featuresFile) throws IOException {
        File iniFile = new File(featuresFile);
        String iniPath = iniFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        for(String feature: parseLines(featuresFile)){
            feature = feature.trim().replace("{INI_PATH}", iniPath);
            String[] splitted = feature.split(":");
            String featureName;
            if(splitted.length > 2 && splitted[1].length() == 1) {
                featureName = splitted[0]+splitted[1];
            }
            else {
                featureName = splitted[0];
            }
            this.features.put(featureName, feature);
        }
        return this.features;
    }

    public List<Pattern> parseTypes(String typesFile) throws IOException {
        for(String type: parseLines(typesFile)){
            this.types.add(Pattern.compile("^"+type+"$"));
        }
        return this.types;
    }

    private ArrayList<String> parseLines(String filepath) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new FileReader(filepath));
        StringBuffer sb = new StringBuffer();
        String line = br.readLine();
        while(line != null) {
            if(!line.startsWith("#")){
                line = line.trim();
                lines.add(line);
                line = br.readLine();
            }
        }
        return lines;
    }

    public void setCVTrainData(String data){
        properties.setProperty("cvData", data);
    }

    public void setCVDataFormat(String format){
        properties.setProperty("cvFormat", format);
    }
}
