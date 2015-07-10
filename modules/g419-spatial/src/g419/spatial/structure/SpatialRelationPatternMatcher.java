package g419.spatial.structure;

import g419.toolbox.sumo.Sumo;

import java.util.LinkedList;
import java.util.List;

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
		
		if ( !relation.getSpatialIndicator().getText().toLowerCase().equals(pattern.getIndicator()) ){
			return false;
		}

		boolean trajector = false;
		boolean landmark = false;

		for ( String concept : pattern.getTrajectorConcepts() ){
			if ( sumo.isClassOrSubclassOf(relation.getTrajectorConcepts(), concept) ){
				trajector = true;
			}
		}

		for ( String concept : pattern.getLandmarkConcepts() ){
			if ( sumo.isClassOrSubclassOf(relation.getLandmarkConcepts(), concept) ){
				landmark = true;
			}
		}

		return trajector && landmark;
	}
	
}
