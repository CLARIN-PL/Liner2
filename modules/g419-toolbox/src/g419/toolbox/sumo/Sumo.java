package g419.toolbox.sumo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 5/29/15.
 */
public class Sumo {

    private final Pattern subclassRelPattern = Pattern.compile("^\\p{Z}*\\(subclass (\\p{L}+) (\\p{L}+)\\)\\p{Z}*$");
    private SumoGraph graph = new SumoGraph();
    private boolean caseSensitive = true;

    public Sumo(String mapping) throws IOException, DataFormatException {
        File mappingFile = new File(mapping);
        if(mappingFile.exists()) {
            parseMapping(new FileInputStream(mappingFile));
        }
        else{
            throw new DataFormatException("Mapping file does not exist: " + mapping);
        }
    }

    public Sumo() throws IOException {
    	this.loadDeafultKifs();
    }

    public Sumo(boolean caseSensitive) throws IOException {
    	this.caseSensitive = caseSensitive;
    	this.loadDeafultKifs();
    }

    public boolean containsClass(String label){
    	if ( this.caseSensitive == false ){
    		label = label.toLowerCase();
    	}
        return graph.containsClass(label);
    }
    
    /**
     * Wczytuje domyślny zestaw kifów znajdujący się w jar.
     * @throws IOException 
     */
    private void loadDeafultKifs() throws IOException{
    	this.parseMapping(getClass().getResourceAsStream("/Merge.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Geography.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Transportation.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Mid-level-ontology.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Economy.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Cars.kif"));
    }

    private void parseMapping(InputStream mapping) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(mapping));
        String line = reader.readLine();
        while(line != null){
            Matcher m = subclassRelPattern.matcher(line);
            if(m.find()){
                String c1 = this.caseSensitive ? m.group(1) : m.group(1).toLowerCase();
                String c2 = this.caseSensitive ? m.group(2) : m.group(2).toLowerCase();
                this.graph.addConnection(c1, c2);
            }
            line = reader.readLine();
        }
    }

    public boolean isSubclassOf(String subClass, String upperClass){
    	if ( this.caseSensitive == false ){
    		subClass = subClass.toLowerCase();
    		upperClass = upperClass.toLowerCase();
    	}
        return graph.isSubclassOf(graph.getNode(subClass), graph.getNode(upperClass));
    }

    public boolean isSubclassOf(Set<String> subclasses, String upperClass){
    	if ( this.caseSensitive == false ){
    		upperClass = upperClass.toLowerCase();
    	}
        for(String subClass: subclasses){
        	if ( this.caseSensitive == false ){
        		subClass = subClass.toLowerCase();
        	}
            if(graph.isSubclassOf(graph.getNode(subClass), graph.getNode(upperClass))){
                return true;
            }
        }
        return false;
    }

    public boolean isClassOrSubclassOf(String subClass, String upperClass){
    	if ( this.caseSensitive == false ){
    		subClass = subClass.toLowerCase();
    		upperClass = upperClass.toLowerCase();
    	}
        if(subClass.equals(upperClass)){
            return true;
        }
        else{
            return isSubclassOf(subClass, upperClass);
        }

    }

    public boolean isClassOrSubclassOf(Set<String> subclasses, String upperClass){
    	if ( this.caseSensitive == false ){
    		//subClass = subClass.toLowerCase();
    		upperClass = upperClass.toLowerCase();
    	}
        if(subclasses.contains(upperClass)){
            return true;
        }
        else{
            return isSubclassOf(subclasses, upperClass);
        }

    }
}
