package g419.toolbox.sumo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 5/29/15.
 */
public class Sumo {

    private final Pattern subclassRelPattern = Pattern.compile("^\\p{Z}*\\(subclass (\\p{L}+) (\\p{L}+)\\)\\p{Z}*$");
    private Graph graph = new Graph();
    private boolean caseSensitive = false;

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
    	this.parseMapping(getClass().getResourceAsStream("/Mid-level-ontology.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Transportation.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Economy.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Cars.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/naics.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Food.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Media.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/TransportDetail.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/Dining.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/QoSontology.kif"));
    	this.parseMapping(getClass().getResourceAsStream("/MilitaryDevices.kif"));
    	
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
    
    /**
     * Zwraca zbiór wszystkich (bezpośrednich i pośrednich) klas nadrzędnych dla wskazanej klasy.
     * @return
     */
    public Set<String> getSuperclasses(String currentClass){
    	return this.graph.getSuperclasses(currentClass);
    }

    public Set<String> getSubclasses(String upperClass){
    	Set<String> subclasses = new HashSet<String>();
   		this.graph.getSubclasses(upperClass, subclasses);
    	return subclasses;
    }

    public Set<String> getSubclasses(Set<String> classes){
    	Set<String> subclasses = new HashSet<String>();
    	for ( String cl : classes ){
    		this.graph.getSubclasses(cl, subclasses);
    	}
    	return subclasses;
    }

    public boolean isSubclassOf(String subClass, String upperClass){
    	if ( this.caseSensitive == false ){
    		subClass = subClass.toLowerCase();
    		upperClass = upperClass.toLowerCase();
    	}
    	return this.graph.isSubclassOf(subClass, upperClass);
    }

    public boolean isSubclassOf(Set<String> subclasses, String upperClass){
    	if ( this.caseSensitive == false ){
    		upperClass = upperClass.toLowerCase();
    	}
        for(String subClass: subclasses){
        	if ( this.caseSensitive == false ){
        		subClass = subClass.toLowerCase();
        	}
        	if(graph.isSubclassOf(subClass, upperClass)){
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
    		upperClass = upperClass.toLowerCase();
    		Set<String> subclassesLower = new HashSet<String>();
    		for ( String cl : subclasses ){
    			subclassesLower.add(cl.toLowerCase());
    		}
    		subclasses = subclassesLower;
    	}
        if(subclasses.contains(upperClass)){
            return true;
        }
        else{
            return isSubclassOf(subclasses, upperClass);
        }

    }
    
}
