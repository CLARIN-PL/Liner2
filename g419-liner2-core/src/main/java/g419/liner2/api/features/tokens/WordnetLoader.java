package g419.liner2.api.features.tokens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRelationRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonIndexRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonParser;

public class WordnetLoader {
	HashMap<String, HashMap<String, PrincetonDataRaw>> data = new HashMap<String, HashMap<String, PrincetonDataRaw>>();
	HashMap<String, HashMap<String, PrincetonIndexRaw>> index = new HashMap<String, HashMap<String, PrincetonIndexRaw>>();
	
	String[][] poses = new String[][] { {"adj", "a"}, {"adv", "r"}, {"noun", "n"},  {"verb", "v"} };
	
	String wordnet_path;
	
	public WordnetLoader(String path){
		wordnet_path = path;
		try {
			if (!new File(path).exists())
				throw new FileNotFoundException("Invalid database directory: "+path);
			for (String pos[] : poses){
			String filename = path + File.separator +  "index." + pos[0]; 
				if ((new File(filename)).exists()){						
					index.put(pos[1], readIndexFile(filename));

					
				}
			}
				for (String pos[] : poses){
		
					String filename = path + File.separator +  "data." + pos[0]; 
					if ((new File(filename)).exists()){						
						data.put(pos[1], readDataFile(filename, pos[1]));
					}
											
				}
				
        } catch (Exception e) {
            e.printStackTrace();
        }
        
		
	}
	
	public HashMap<String, PrincetonIndexRaw> readIndexFile(String filename) throws IOException{
		HashMap<String, PrincetonIndexRaw> units = new HashMap<String, PrincetonIndexRaw>();
		
		BufferedReader r = new BufferedReader(new FileReader(filename));
		
		String line = null;
		while (( line = r.readLine()) != null){
			// Skip lines with comments. Lines with comments starts with two spaces.
			if (line.startsWith("  ")) continue;
			// Parse line
			PrincetonIndexRaw raw = PrincetonParser.parseIndexLine(line);
			units.put(raw.lemma, raw);
		}
		r.close();
		return units;
	}
	
	public HashMap<String, PrincetonDataRaw> readDataFile(String filename, String pos) throws IOException{		
		BufferedReader r = new BufferedReader(new FileReader(filename));		
		HashMap<String, PrincetonDataRaw> data = new HashMap<String, PrincetonDataRaw>();
		
		String line = null;
		while (( line = r.readLine()) != null){
			// Skip lines with comments. Lines with comments starts with two spaces.
			if (line.startsWith("  ")) continue;
			// Parse line
			PrincetonDataRaw d = PrincetonParser.parseDataLine(line);
			d.pos = pos;
			data.put(d.offset, d);			
		}
		r.close();
		
		
		return data;
	}
	
	public ArrayList<PrincetonDataRaw> getSynsets(String word){
		ArrayList<PrincetonDataRaw> synsets = new ArrayList<PrincetonDataRaw>();
		for(Entry<String, HashMap<String, PrincetonIndexRaw>> units: index.entrySet())
			if(units.getValue().containsKey(word))
				for(String offset: units.getValue().get(word).synset_offsets)
					synsets.add(data.get(units.getKey()).get(offset));
		return synsets;	
	}
	
	public ArrayList<PrincetonDataRaw> getHypernyms(PrincetonDataRaw synset){
		ArrayList<PrincetonDataRaw> hypernyms = new ArrayList<PrincetonDataRaw>();
		for(PrincetonDataRelationRaw rel: synset.relations)
			if(rel.type.equals("@"))
				hypernyms.add(data.get(rel.pos).get(rel.offset));
		return hypernyms;	
	
	}

}
