package g419.toolbox.sumo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Klasa reprezentuje mapowanie kategorii jednostek identyfikacyjnych na koncepcje sumo.
 * 
 * @author czuk
 *
 */
public class NamToSumo {

	private Sumo sumo = null;
	private Map<String, Set<String>> mapping = new HashMap<String, Set<String>>();
	
	/**
	 * Tworzy domyślne mapowanie z wykorzystaniem domyślnej ontologii SUMO.
	 * @throws IOException
	 */
	public NamToSumo() throws IOException{
		this.sumo = new Sumo(false);
		Reader reader = new InputStreamReader(getClass().getResourceAsStream("/nam2sumo.txt"));
		this.parse(reader);
	}

	/**
	 * Mapowanie wczytywanie jest ze strumienia reader, w którym każda linia zawiera mapowanie
	 * jednej kategorii na listę konceptów z SUMO. Przykład linii z mapowaniem:
	 *   nam_org_company	Business, CommercialAgent 
	 * Nazwa kategorii i lista konceptów oddzielone są znakiem tabulacji. Koncepty oddzielone są przecinkami.
	 * Przy wczytywaniu nazwy kategorii i konceptów rzutowane są do małych liter.
	 * 
	 * @param reader Strumień z którego zostanie wczytanie mapowanie.
	 * @param sumo Obiekt reprezentujący ontologię sumo do sprawdzenia, czy wczytane koncepty istnieją w ontologii. 
	 * @throws IOException 
	 */
	public NamToSumo(Reader reader, Sumo sumo) throws IOException{
	}
	
	public void parse(Reader reader) throws IOException{
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while ( (line = br.readLine()) != null ){
			line = line.trim();
			String[] cols = line.split("\t");
			if ( cols.length == 2 ){
				String name = cols[0];
				String[] concepts = cols[1].split("[ ]*,[ ]*");
				Set<String> conceptsSet = this.mapping.get(name);
				if ( conceptsSet == null ){
					conceptsSet = new HashSet<String>();
					this.mapping.put(name, conceptsSet);		
				}
				for ( String concept : concepts ){
					concept = concept.toLowerCase();
					conceptsSet.add(concept);
					if ( !this.sumo.containsClass(concept)){
						Logger.getLogger(this.getClass()).warn(String.format("Concept '%s' not found in SUMO", concept));
					}					
				}
			}
		}
	}
	
    public Set<String> getConcept(String word){
        return mapping.get(word);
    } 
	
}