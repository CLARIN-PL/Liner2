package g419.toolbox.sumo;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 5/29/15.
 */
public class Sumo {

    private final Pattern subclassRelPattern = Pattern.compile("^\\p{Z}*\\(subclass (\\p{L}+) (\\p{L}+)\\)\\p{Z}*$");
    private SumoGraph graph;



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
            parseMapping(getClass().getResourceAsStream("/Merge.kif"));
    }

    public boolean containsClass(String label){
        return graph.containsClass(label);
    }

    private void parseMapping(InputStream mapping) throws IOException {
        graph = new SumoGraph();
        BufferedReader reader = new BufferedReader(new InputStreamReader(mapping));
        String line = reader.readLine();
        while(line != null){
            Matcher m = subclassRelPattern.matcher(line);
            if(m.find()){
                graph.addConnection(m.group(1), m.group(2));
            }
            line = reader.readLine();
        }
    }

    public boolean isSubclassOf(String subClass, String upperClass){
        return graph.isSubclassOf(graph.getNode(subClass), graph.getNode(upperClass));
    }

    public boolean isSubclassOf(Set<String> subclasses, String upperClass){
        for(String subClass: subclasses){
            if(graph.isSubclassOf(graph.getNode(subClass), graph.getNode(upperClass))){
                return true;
            }
        }
        return false;
    }

    public boolean isClassOrSubclassOf(String subClass, String upperClass){
        if(subClass.equals(upperClass)){
            return true;
        }
        else{
            return isSubclassOf(subClass, upperClass);
        }

    }

    public boolean isClassOrSubclassOf(Set<String> subclasses, String upperClass){
        if(subclasses.contains(upperClass)){
            return true;
        }
        else{
            return isSubclassOf(subclasses, upperClass);
        }

    }
}
