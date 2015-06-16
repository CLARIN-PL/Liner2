package g419.toolbox.sumo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 6/2/15.
 */
public class WordnetToSumo {

    private Pattern synsetUnitPatern = Pattern.compile("(\\{|\\p{Z})(([^\\p{Z}]+)-([0-9]+))");
    private HashMap<String, HashSet<String>> wordMapping;
    private HashMap<String, HashSet<String>> wordSenseMapping;

    /**
     * Tworzy obiekt na podstawie pliku z mapowaniem Serdela.
     * @param serdelMapping
     * @throws IOException
     * @throws DataFormatException
     */
    public WordnetToSumo(String serdelMapping) throws IOException, DataFormatException {
        wordMapping = new HashMap<String, HashSet<String>>();
        wordSenseMapping = new HashMap<String, HashSet<String>>();
        File mapping = new File(serdelMapping);
        if(mapping.exists()){
        	Reader serdelReader = null;
        	serdelReader = new FileReader(mapping);
            parseMapping(serdelReader);
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
        wordMapping = new HashMap<String, HashSet<String>>();
        wordSenseMapping = new HashMap<String, HashSet<String>>();
        parseMapping(serdelReader);
    }

    public Set<String> getConcept(String word){
        return wordMapping.get(word);
    }

    public Set<String> getConcept(String word, int sense){
        return wordSenseMapping.get(word+"-"+sense);
    }

    private void parseMapping(Reader mappingReader) throws IOException, DataFormatException {
        BufferedReader reader = new BufferedReader(mappingReader);
        String line = reader.readLine();
        while(line != null){
            String[] attrs = line.split(";");
            if(!attrs[attrs.length - 1].equals("R")){
                String sumoClass = attrs[attrs.length - 2];
                String synset = attrs[2];
                HashMap<String, String> synsetUnits = parseSynset(synset);
                for(String wordAndSense: synsetUnits.keySet()){
                    if(wordAndSense.equals("a-1")){
                        System.out.println(synset);
                    }
                    addMapping(wordAndSense, sumoClass, wordSenseMapping);
                    addMapping(synsetUnits.get(wordAndSense), sumoClass, wordMapping);
                }
            }
            line = reader.readLine();
        }
    }

    private void addMapping(String key, String sumoClass, HashMap<String, HashSet<String>> mapping){
        if(mapping.containsKey(key)){
            mapping.get(key).add(sumoClass);
//            System.out.println(key + " MULTIPLE CLASSES: " + Arrays.toString(mapping.get(key).toArray()));
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
