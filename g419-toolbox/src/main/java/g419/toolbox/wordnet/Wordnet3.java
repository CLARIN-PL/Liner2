package g419.toolbox.wordnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataLemmaRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRelationRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonIndexRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonParser;

/**
 * Następca Wordnet i Wordnet2.
 * TODO wymagana integracja Wordnet, Wordnet2 i Wordnet3
 * @author czuk
 *
 */
public class Wordnet3 {
	HashMap<String, HashMap<String, PrincetonDataRaw>> data = new HashMap<String, HashMap<String, PrincetonDataRaw>>();
	HashMap<String, HashMap<String, PrincetonIndexRaw>> index = new HashMap<String, HashMap<String, PrincetonIndexRaw>>();
	
	public final static String REL_HYPERNYM = "@";
	public final static String REL_HYPONYM = "~";
	public final static String REL_MERONYM = "%";
	public final static String REL_HOLONYM = "#";
	
	String[][] poses = new String[][] { {"adj", "a"}, {"adv", "r"}, {"noun", "n"},  {"verb", "v"} };
	
	String wordnet_path;
	
	/**
	 * Tworzy obiekt na podstawie plików w formacie Princeton.
	 * @param path Ścieżka do katalogu z plikami w formacie Princeton.
	 */
	public Wordnet3(String path){
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
	
	/**
	 * 
	 * @return
	 */
	public List<PrincetonDataRaw> getSynsets(){
		List<PrincetonDataRaw> synsets = new ArrayList<PrincetonDataRaw>();
		for ( Map<String, PrincetonDataRaw> map : this.data.values() ){
			synsets.addAll(map.values());
		}
		return synsets;
	}
	
	/**
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
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
	
	/**
	 * 
	 * @param filename
	 * @param pos
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * Zwraca listę jednostek leksykalnych przypisanych do danego synsetu.
	 * @param synset
	 * @return
	 */
	public List<String> getLexicalUnits(PrincetonDataRaw synset){
		List<String> lemmas = new ArrayList<String>();
		for (PrincetonDataLemmaRaw lemma : synset.lemmas)
			lemmas.add(lemma.lemma);
		return lemmas;
	}

	/**
	 * Zwraca zbiór systenów bezpośrednio połączonych relacją typu relation z synstem synset.
	 * @param synset Obiekt reprezentujący synset
	 * @param relation Typ relacji
	 * @return
	 */
	public Set<PrincetonDataRaw> getDirectSynsets(PrincetonDataRaw synset, String relation){
		Set<PrincetonDataRaw> synstens = new HashSet<PrincetonDataRaw>();
		for(PrincetonDataRelationRaw rel: synset.relations)
			if(rel.type.startsWith(relation)){
				PrincetonDataRaw direct = data.get(rel.pos).get(rel.offset);
				if ( direct != null ){
					synstens.add(direct);
				}
			}
		return synstens;		
		
	}
	
	/**
	 * Zwraca zbiór wszystkich systenów połączonych (bezpośrednio lub pośrednio) relacją 
	 * typu relation z synstem synset.
	 * @param synset Obiekt reprezentujący synset
	 * @param relation Typ relacji
	 * @return
	 */
	public Set<PrincetonDataRaw> getAllSynsets(PrincetonDataRaw synset, String relation){
		Set<PrincetonDataRaw> synsets = new HashSet<PrincetonDataRaw>();
		this.getAllSynsets(synset, relation, synsets);
		return synsets;		
		
	}

	/**
	 * Zwraca zbiór wszystkich systenów połączonych (bezpośrednio lub pośrednio) relacją 
	 * typu relation z synstem synset.
	 * @param synset Obiekt reprezentujący synset
	 * @param relation Typ relacji
	 */
	public void getAllSynsets(PrincetonDataRaw synset, String relation, Set<PrincetonDataRaw> synsets){
		for ( PrincetonDataRaw synsetByRel : this.getDirectSynsets(synset, relation)){
			if ( !synsets.contains(synsetByRel) ){
				synsets.add(synsetByRel);
				this.getAllSynsets(synsetByRel, relation, synsets);
			}
		}
	}
	
	/**
	 * 
	 * @param word
	 * @return
	 */
	public List<PrincetonDataRaw> getSynsets(String word){
		ArrayList<PrincetonDataRaw> synsets = new ArrayList<PrincetonDataRaw>();
		for(Entry<String, HashMap<String, PrincetonIndexRaw>> units: index.entrySet())
			if(units.getValue().containsKey(word))
				for(String offset: units.getValue().get(word).synset_offsets)
					synsets.add(data.get(units.getKey()).get(offset));
		return synsets;	
	}

	/**
	 * Zwraca synsety zawierające jednostkę leksykalną o lemacie word i sensie sense.
	 * @param word
	 * @param sense
	 * @return
	 */
	public List<PrincetonDataRaw> getSynsets(String word, int sense){
		ArrayList<PrincetonDataRaw> synsets = new ArrayList<PrincetonDataRaw>();
		for(Entry<String, HashMap<String, PrincetonIndexRaw>> units: index.entrySet())
			if(units.getValue().containsKey(word))
				if (units.getValue().get(word).synset_offsets.size() >= sense){
					String offset = units.getValue().get(word).synset_offsets.get(sense-1); 
					synsets.add(data.get(units.getKey()).get(offset));
				}
		return synsets;	
	}

	/**
	 * 
	 * @param synset
	 * @return
	 */
	public Set<PrincetonDataRaw> getDirectHypernyms(PrincetonDataRaw synset){
		return this.getDirectSynsets(synset, Wordnet3.REL_HYPERNYM);
	}

	/**
	 * 
	 * @param synset
	 * @return
	 */
	public Set<PrincetonDataRaw> getDirectHyponyms(PrincetonDataRaw synset){
		return this.getDirectSynsets(synset, Wordnet3.REL_HYPONYM);
	}

	/**
	 * 
	 * @param synset
	 * @return
	 */
	public Set<PrincetonDataRaw> getDirectHolonyms(PrincetonDataRaw synset){
		return this.getDirectSynsets(synset, Wordnet3.REL_HOLONYM);
	}

	/**
	 * 
	 * @param synset
	 * @return
	 */
	public Set<PrincetonDataRaw> getDirectMeronyms(PrincetonDataRaw synset){
		return this.getDirectSynsets(synset, Wordnet3.REL_MERONYM);
	}
	
	/**
	 * 
	 * @param synset
	 * @return
	 */
	public Set<PrincetonDataRaw> getAllHolonyms(PrincetonDataRaw synset){
		return this.getAllSynsets(synset, Wordnet3.REL_HOLONYM);
	}

	/**
	 * Zwraca wszystkie (bezpośrednie i pośrednie holonimy danego synsetu).
	 * Holonim bezpośredni, to synset połączony relacją holonimii z danym synsetem.
	 * Holonim pośredni to:
	 * a) holonim holonimu danego synsetu i kolejne poziomy,
	 * b) holonim każdego hiperonimu synsetu,
	 * c) 
	 * @param synset
	 * @param meronyms
	 */
	public void getHolonyms(PrincetonDataRaw synset, Set<PrincetonDataRaw> holonyms, boolean takeHypernyms, boolean takeHyponyms){
		Set<PrincetonDataRaw> directHypernyms = this.getDirectHypernyms(synset);
		Set<PrincetonDataRaw> directHolonyms = this.getDirectHolonyms(synset);
		
		for ( PrincetonDataRaw synsetLinked : directHolonyms ){
			if ( !holonyms.contains(synsetLinked) && synsetLinked.domain.equals(synset.domain) ){
				holonyms.add(synsetLinked);
				//for ( PrincetonDataRaw)
				this.getHolonyms(synsetLinked, holonyms, true, true);
				if ( takeHyponyms ){
					for ( PrincetonDataRaw hyponym : this.getDirectHyponyms(synsetLinked) ){
						if ( !(holonyms.contains(hyponym)) && hyponym.domain.equals(synset.domain) ){
							holonyms.add(hyponym);
							this.getHolonyms(hyponym, holonyms, false, true);
						}
					}
				}
			}
		}

		if ( takeHypernyms ){
			for ( PrincetonDataRaw synsetLinked : directHypernyms ){
				if ( !holonyms.contains(synsetLinked) && synsetLinked.domain.equals(synset.domain) ){
					holonyms.add(synsetLinked);
					//for ( PrincetonDataRaw)
					this.getHolonyms(synsetLinked, holonyms, true, false);
				}
			}
		}
	}
	

	public Set<PrincetonDataRaw> getHolonyms(PrincetonDataRaw synset){
		Set<PrincetonDataRaw> holonyms = new HashSet<PrincetonDataRaw>();
		this.getHolonyms(synset, holonyms, true, true);
		return holonyms;
	}


}