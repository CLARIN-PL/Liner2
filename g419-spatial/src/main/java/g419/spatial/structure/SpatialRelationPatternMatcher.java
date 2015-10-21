package g419.spatial.structure;

import g419.toolbox.sumo.Sumo;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.maltparser.core.helper.HashSet;

public class SpatialRelationPatternMatcher {

	private List<SpatialRelationPattern> patterns = null;
	private Sumo sumo = null;
	
	public SpatialRelationPatternMatcher(List<SpatialRelationPattern> patterns, Sumo sumo){
		this.patterns = patterns;
		this.sumo = sumo;
	}
	
	/**
	 * Dopasowuje wszystkie wzorce, do których pasuje wyrażenie przestrzenne.
	 * @param relation
	 * @return
	 */
	public List<SpatialRelationPattern> matchAll(SpatialRelation relation){
		List<SpatialRelationPattern> matching = new LinkedList<SpatialRelationPattern>();
		
		for ( SpatialRelationPattern pattern : this.patterns ){
			if ( SpatialRelationPatternMatcher.matches(relation, pattern, this.sumo)){
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
	public static boolean matches(SpatialRelation relation, SpatialRelationPattern pattern, Sumo sumo){
		
		if ( !pattern.getIndicators().contains(relation.getSpatialIndicator().getText().toLowerCase()) ){
			return false;
		}

		boolean trajector = false;
		boolean landmark = false;
		
		String[] parts = relation.getLandmark().getHeadToken().getDisambTag().getCtag().split(":"); 
		if ( parts.length > 2 && !parts[2].equals("loc") ){
			//Logger.getLogger(SpatialRelationPatternMatcher.class).info("Landmark not locative");
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

		
//		for ( String concept : pattern.getTrajectorConcepts() ){
//			if ( sumo.isClassOrSubclassOf(relation.getTrajectorConcepts(), concept) ){
//				trajector = true;
//			}
//		}
//
//		for ( String concept : pattern.getLandmarkConcepts() ){
//			if ( sumo.isClassOrSubclassOf(relation.getLandmarkConcepts(), concept) ){
//				landmark = true;
//			}
//		}

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
