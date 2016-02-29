package g419.spatial.io;

import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.structure.SpatialRelationSchemaMatcher;
import g419.toolbox.sumo.Sumo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

/**
 * Klasa do parsowania schematów ontologicznych wyrażeń przestrzennych zapisanych w formacie CSV.
 * Przykładowy plik: g419-spatial/src/main/resources/g419/spatial/resources/spatial_schemes.csv
 * @author czuk
 *
 */
public class CsvSpatialSchemeParser {

	private BufferedReader reader = null;
	private Sumo sumo = null;
	private Logger logger = Logger.getLogger(CsvSpatialSchemeParser.class); 
	private boolean general = true;

	public CsvSpatialSchemeParser(Reader reader, Sumo sumo){
		this.reader = new BufferedReader(reader);
		this.sumo = sumo;
	}

	/**
	 * 
	 * @param reader Reader z którego będą wczytywane schematy
	 * @param sumo Obiekt SUMO do sprawdzenia poprawności konceptów
	 * @param general Czy użyć ogólnych konceptów (true), czy szczegółowych (false)
	 */
	public CsvSpatialSchemeParser(Reader reader, Sumo sumo, boolean general){
		this.reader = new BufferedReader(reader);
		this.sumo = sumo;
		this.general = general;
	}
	
	/**
	 * Parsuje schematy
	 * @return 
	 * @throws IOException
	 */
	public SpatialRelationSchemaMatcher parse() throws IOException{
		// Kolumny zawierające uogólnione koncepty dla trajektorów i landmarków
		int columnTrajector = 12;
		int columnLandmark = 14;
		
		// Kolumny zawierające uszczegółowione koncepty dla trajektorów i landmarków
		if ( !this.general ){
			columnTrajector = 13;
			columnLandmark = 15;			
		}
		List<SpatialRelationSchema> patterns = new LinkedList<SpatialRelationSchema>();
		
		CSVParser csv = new CSVParser(this.reader, CSVFormat.DEFAULT);
		
		Iterator<CSVRecord> it = csv.iterator();

		// Pomiń nagłówki
		it.next();
		it.next();
				
		while ( it.hasNext() ){
			CSVRecord record = it.next();
			if ( record.size() < 15 || record.get(0).trim().length() == 0 ){
				continue;
			}
			
			String preposition = record.get(0).trim().toLowerCase();
			String id = preposition + "#" + record.get(1);
			String cas = record.get(2).trim();
			boolean use = record.get(3).toLowerCase().trim().equals("t");
			
			if ( !use ){
				//Logger.getLogger(this.getClass()).info(String.format("Schemat %s został pominięty (use=nie)", id));
				continue;
			}
			
			if ( cas.length() == 0 ){
				Logger.getLogger(this.getClass()).info(String.format("Przypadek nie został określony (use=nie)", id));
				continue;
			}

			String[] trajectorIds = record.get(columnTrajector).trim().split("( )*,( )*");
			String[] landmarkIds = record.get(columnLandmark).trim().split("( )*,( )*");
			
			if ( record.get(columnTrajector).trim().length() == 0 || trajectorIds.length == 0 ){
				Logger.getLogger(this.getClass()).warn(String.format("Pusty zbiór trajector (schemat %s)", id));
				continue;
			}

			if ( record.get(columnLandmark).trim().length() == 0 || landmarkIds.length == 0 ){
				Logger.getLogger(this.getClass()).warn(String.format("Pusty zbiór landmark (schemat %s)", id));
				continue;
			}

			Set<String> sis = new HashSet<String>();
			for ( String si : preposition.split(",") ){
				sis.add(si.trim());
			}

			Set<String> trajectorConcepts = this.parseConcepts(id, trajectorIds);
			Set<String> landmarkConcepts = this.parseConcepts(id, landmarkIds);
			
			if ( trajectorConcepts.size() > 0 && landmarkConcepts.size() > 0 ){
				patterns.add(new SpatialRelationSchema(id, cas, sis, trajectorConcepts, landmarkConcepts));
			}
			else if ( trajectorConcepts.size() == 0 ){
				Logger.getLogger(this.getClass()).warn(String.format("Pusty zbiór trajector (schemat %s)", id));
			}
			else if ( landmarkConcepts.size() == 0 ){
				Logger.getLogger(this.getClass()).warn(String.format("Pusty zbiór landmark (schemat %s)", id));
			}
			
		}
		csv.close();
		
		logger.info(String.format("Liczba wczytanych wzorców: %d", patterns.size()));
		return new SpatialRelationSchemaMatcher(patterns, this.sumo);
	}	
	
	/**
	 * 
	 * @param id
	 * @param conceptsIds
	 * @return
	 */
	private Set<String> parseConcepts(String id, String[] conceptsIds){
		Set<String> concepts = new HashSet<String>();
		for (String part : conceptsIds ){
			if ( part.length() < 2 ){
				Logger.getLogger(this.getClass()).warn(String.format("Niepoprawna nazwa konceptu; koncepty: %s (schemat %s)", String.join(", ", conceptsIds), id));				
			}
			else if ( !part.startsWith("#") ){
				Logger.getLogger(this.getClass()).warn(String.format("Nazwa konceptu nie zaczyna się od #: %s (schemat %s)", part, id));
			}
			else{
				part = part.trim().substring(1);
				if ( this.sumo.containsClass(part) ){
					concepts.add(part);
				}
				else{
					Logger.getLogger(this.getClass()).warn(String.format("Koncept '%s' nie został znaleziony w SUMO (schemat %s)", part, id));
				}
			}
		}
		return concepts;
	}
	
}
