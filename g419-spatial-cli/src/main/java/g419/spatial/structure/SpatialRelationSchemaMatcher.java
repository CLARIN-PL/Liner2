package g419.spatial.structure;

import g419.corpus.structure.Token;
import g419.toolbox.sumo.Sumo;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.maltparser.core.helper.HashSet;

public class SpatialRelationSchemaMatcher {

	private List<SpatialRelationSchema> patterns = null;
	private Sumo sumo = null;
	
	public SpatialRelationSchemaMatcher(List<SpatialRelationSchema> patterns, Sumo sumo){
		this.patterns = patterns;
		this.sumo = sumo;
	}
	
	/**
	 * Dopasowuje wszystkie wzorce, do których pasuje wyrażenie przestrzenne.
	 * @param relation
	 * @return
	 */
	public List<SpatialRelationSchema> matchAll(SpatialRelation relation){
		List<SpatialRelationSchema> matching = new LinkedList<SpatialRelationSchema>();
		
		for ( SpatialRelationSchema pattern : this.patterns ){
			if ( SpatialRelationSchemaMatcher.matches(relation, pattern, this.sumo)){
				matching.add(pattern);
			}
		}
		
		return matching;
	}
	
	/**
	 * Sprawdza, czy relacja relation pasuje do wzorca pattern dla ontologii sumo.
	 * @param relation
	 * @param pattern
	 * @param sumo
	 * @return
	 */
	public static boolean matches(SpatialRelation relation, SpatialRelationSchema pattern, Sumo sumo){

		String preposition = relation.getSpatialIndicator().getText().toLowerCase();
		if ( relation.getRegion() != null && relation.getRegion().getHeadToken().getDisambTag().getBase().equals("teren") ){
			// Zamiana przyimka z "na" na "w" dla region=teren
			preposition = "w";
		}

		if ( !pattern.getIndicators().contains(preposition) ){
			return false;
		}

		boolean trajector = false;
		boolean landmark = false;
		Token checkTokenPos = relation.getRegion() != null ? relation.getRegion().getHeadToken() : relation.getLandmark().getHeadToken(); 
		
		String[] parts = checkTokenPos.getDisambTag().getCtag().split(":"); 
		if ( parts.length > 2 && !parts[2].equals(pattern.getCase()) ){
			return false;
		}

		Set<String> landmarkSubclasses = new HashSet<String>();
		Set<String> trajectorSubclasses = new HashSet<String>();
		
		for (String str : pattern.getLandmarkConcepts() ){
			landmarkSubclasses.addAll(sumo.getSubclasses(str.toLowerCase()));
			landmarkSubclasses.add(str.toLowerCase());
		}

		for (String str : pattern.getTrajectorConcepts() ){
			trajectorSubclasses.addAll(sumo.getSubclasses(str.toLowerCase()));
			trajectorSubclasses.add(str.toLowerCase());
		}

		
		for ( String concept : relation.getTrajectorConcepts() ){
			if ( trajectorSubclasses.contains(concept.toLowerCase()) ){
				trajector = true;
			}
		}
				
		for ( String concept : relation.getLandmarkConcepts() ){
			if ( landmarkSubclasses.contains(concept.toLowerCase()) ){
				landmark = true;
			}
		}
				
		return trajector && landmark;
	}
	
}
