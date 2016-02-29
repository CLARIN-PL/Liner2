package g419.toolbox.sumo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

/**
 * 
 */
public class WordnetToSumo {

    private Pattern synsetUnitPatern = Pattern.compile("(\\{|\\p{Z})(([^\\p{Z}]+)-([0-9]+))");
    /* Mapowanie lematów słów w zbiór pojęć */
    private HashMap<String, Set<String>> wordMapping = null;
    
    /* Mapowanie lemat+sens, np. firma-1 w zbiór pojęć */
    private HashMap<String, Set<String>> wordSenseMapping;
    
    /* Mapowanie id synsetu w zbiór pojęć */
    private HashMap<String, Set<String>> synsetIdMapping = null;

    /**
     * Wczytuje mapowanie z domyślnym modelem, tj. mapping-26.05.2015-Serdel.csv.gz
     * @throws IOException
     * @throws DataFormatException
     */
    public WordnetToSumo() throws IOException, DataFormatException {
    	this.wordMapping = new HashMap<String, Set<String>>();
    	this.wordSenseMapping = new HashMap<String, Set<String>>();
    	this.synsetIdMapping = new HashMap<String, Set<String>>();
    	
		//String location = "/mapping-26.05.2015-Serdel.csv.gz";
		String location = "/mapping-28.01.2016-Serdel.csv.gz";
		InputStream resource = this.getClass().getResourceAsStream(location);
		GZIPInputStream gzip = new GZIPInputStream(resource);
	
	    if (resource == null) {
	        throw new MissingResourceException("Resource not found: " + location,
	                this.getClass().getName(), location);
	    }
	    Reader serdelReader = new InputStreamReader( gzip );
	    this.parseMapping(serdelReader);
	    serdelReader.close();
    }
    
    
    /**
     * Tworzy obiekt na podstawie pliku z mapowaniem Serdela.
     * @param serdelMapping
     * @throws IOException
     * @throws DataFormatException
     */
    public WordnetToSumo(String serdelMapping) throws IOException, DataFormatException {
    	this.wordMapping = new HashMap<String, Set<String>>();
    	this.wordSenseMapping = new HashMap<String, Set<String>>();
        File mapping = new File(serdelMapping);
        if(mapping.exists()){
        	Reader serdelReader = null;
        	serdelReader = new FileReader(mapping);
        	this.parseMapping(serdelReader);
            if ( serdelReader != null ){
            	serdelReader.close();
            }
        }
        else{
            throw new DataFormatException("Serdel mapping file does not exist: " + serdelMapping);
        }        
    }

    /**
     * Tworzy obiekt na podstawie strumienia z mapowaniem Serdela.
     * @param serdelReader
     * @throws IOException
     * @throws DataFormatException
     */
    public WordnetToSumo(Reader serdelReader) throws IOException, DataFormatException {
    	this.wordMapping = new HashMap<String, Set<String>>();
    	this.wordSenseMapping = new HashMap<String, Set<String>>();
        this.parseMapping(serdelReader);
    }

    public Set<String> getLemmaConcepts(String word){
        return wordMapping.get(word);
    }

    public Set<String> getConcept(String word, int sense){
        return wordSenseMapping.get(word+"-"+sense);
    }
    
    public Set<String> getSynsetConcepts(String synsetId){
    	return this.synsetIdMapping.get(synsetId);
    }

    private void parseMapping(Reader mappingReader) throws IOException, DataFormatException {
        BufferedReader reader = new BufferedReader(mappingReader);
        /* Pierwsze linia to nagłówek, więc pomijam */
        String line = reader.readLine();
        while( (line = reader.readLine()) != null){
            String[] attrs = line.split(";");
            if( attrs.length > 3 ){ //&& !attrs[attrs.length - 1].equals("R")){
                String synsetId = attrs[0].trim();
            	String sumoClass = attrs[attrs.length - 2];
                String synset = attrs[2];
                HashMap<String, String> synsetUnits = parseSynset(synset);
                /* Dodaj mapowanie dla lematów i lematów+sene */
                for(String wordAndSense: synsetUnits.keySet()){
                    addMapping(wordAndSense, sumoClass, wordSenseMapping);
                    addMapping(synsetUnits.get(wordAndSense), sumoClass, wordMapping);
                }
                /* Dodaj mapowanie dla identyfikatora synsetu */
                Set<String> classes = this.synsetIdMapping.get(synsetId);
                if ( classes == null ){
                	classes = new HashSet<String>();
                	this.synsetIdMapping.put(synsetId, classes);
                }
                classes.add(sumoClass);
            }
        }
    }

    private void addMapping(String key, String sumoClass, HashMap<String, Set<String>> mapping){
        if(mapping.containsKey(key)){
            mapping.get(key).add(sumoClass);
        }
        else{
            HashSet<String> classes = new HashSet<String>();
            classes.add(sumoClass);
            mapping.put(key, classes);
        }
    }

    private HashMap<String, String> parseSynset(String synset){
        HashMap<String, String> units = new HashMap<>();
        Matcher m = synsetUnitPatern.matcher(synset);
        while(m.find()){
            units.put(m.group(2), m.group(3));
        }
        return units;
    }
}
