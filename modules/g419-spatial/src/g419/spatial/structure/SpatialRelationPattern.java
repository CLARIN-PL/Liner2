package g419.spatial.structure;

import java.util.Set;

public class SpatialRelationPattern {

	String indicator = null;
	Set<String> landmarkConcepts = null;
	Set<String> trajectorConcepts = null;
	
	public SpatialRelationPattern(String indicator, Set<String> landmarkConcepts, Set<String> trajectorConcepts){
		this.indicator = indicator;
		this.landmarkConcepts = landmarkConcepts;
		this.trajectorConcepts = trajectorConcepts;
	}
	
	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public Set<String> getLandmarkConcepts() {
		return landmarkConcepts;
	}

	public void setLandmarkConcepts(Set<String> landmarkConcepts) {
		this.landmarkConcepts = landmarkConcepts;
	}

	public Set<String> getTrajectorConcepts() {
		return trajectorConcepts;
	}

	public void setTrajectorConcepts(Set<String> trajectorConcepts) {
		this.trajectorConcepts = trajectorConcepts;
	}

}
