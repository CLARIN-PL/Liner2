package g419.toolbox.wordnet;

import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Klasa reprezentuje mapowanie kategorii jednostek identyfikacyjnych na koncepcje sumo.
 * 
 * @author czuk
 *
 */
public class NamToWordnet {

	private Wordnet3 wordnet = null;
	private Map<String, Set<String>> mapping = new HashMap<String, Set<String>>();

	public NamToWordnet(Wordnet3 wordnet) throws IOException{
		this.wordnet = wordnet;
		Reader reader = new InputStreamReader(getClass().getResourceAsStream("/nam2wordnet.txt"));
		this.parse(reader);
	}

	public NamToWordnet(Reader reader, Wordnet3 wordnet) throws IOException{
		this.wordnet = wordnet;
		this.parse(reader);
	}
	
	public void parse(Reader reader) throws IOException{
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while ( (line = br.readLine()) != null ){
			line = line.trim();
			String[] cols = line.split("\t");
			if ( cols.length == 2 ){
				String type = cols[0];
				String[] lexicalUnits = cols[1].split("[ ]*,[ ]*");
				Set<String> lus = new HashSet<String>();
				for ( String lu : lexicalUnits ){
					String word = lu.substring(0, Math.max(lu.lastIndexOf(' '), 0));
					if ( word.length() > 0 ){
						lus.add(word);
					}
				}
				this.mapping.put(type, lus);
			}
		}
	}
	
    public Set<PrincetonDataRaw> getSynsets(String type){
    	Set<PrincetonDataRaw> synsets = new HashSet<PrincetonDataRaw>();
    	if ( this.mapping.containsKey(type) ){
    		for ( String word : this.mapping.get(type) ){
    			synsets.addAll(this.wordnet.getSynsets(word));
    		}
    	}
        return synsets;
    } 
	
}