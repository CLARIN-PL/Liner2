package g419.liner2.core.tools;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * TODO opis
 * @author czuk
 *
 */
public class TypedDictionary {

	private Map<String, Set<String>> entries = new HashMap<String, Set<String>>();
	
	/**
	 * TODO
	 * @param entry
	 * @param type
	 */
	public void add(String entry, String type){
		Set<String> types = this.entries.get(entry);
		if ( types == null ){
			types = new HashSet<String>();
			this.entries.put(entry, types);
		}
		types.add(type);
	}
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public Set<String> getTypes(String entry){
		return this.entries.get(entry);
	}
	
	/**
	 * TODO
	 * @param filename
	 * @return
	 */
	public static TypedDictionary loadFromFile(String filename){
		TypedDictionary dict = new TypedDictionary();
		BufferedReader bufferreader = null;
	    try {

	        bufferreader = new BufferedReader(new FileReader(filename));
	        String line = bufferreader.readLine();

	        while (line != null) {     
	          	line = line.trim();
	          	if ( line.length() > 0 && !line.startsWith("#") ){
	          		String[] parts = line.split("\t");
	          		if ( parts.length == 2 ){
	          			dict.add(parts[1], parts[0]);
	          		} else {
	          			Logger.getLogger(TypedDictionary.class).error("Invalid line format: " + line);
	          		}
	          	}
	            line = bufferreader.readLine();
	        }	        

	    } catch (FileNotFoundException ex) {
	        Logger.getLogger(TypedDictionary.class).error(ex.getMessage(), ex);
	    } catch (IOException ex) {
	    	Logger.getLogger(TypedDictionary.class).error(ex.getMessage(), ex);
	    } finally {
	    	if ( bufferreader != null ){
	    		try {
					bufferreader.close();
				} catch (IOException ex) {
					Logger.getLogger(TypedDictionary.class).error(ex.getMessage(), ex);
				}
	    	}
	    }
		return dict;
	}
	
}
