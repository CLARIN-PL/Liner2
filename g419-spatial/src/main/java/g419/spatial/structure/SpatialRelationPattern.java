package g419.spatial.structure;

import java.util.Set;

public class SpatialRelationPattern {

	Set<String> indicators = null;
	Set<String> landmarkConcepts = null;
	Set<String> trajectorConcepts = null;
	
	public SpatialRelationPattern(Set<String> indicators, Set<String> trajectorConcepts, Set<String> landmarkConcepts){
		this.indicators = indicators;
		this.landmarkConcepts = landmarkConcepts;
		this.trajectorConcepts = trajectorConcepts;
	}
	
	public Set<String> getIndicators() {
		return indicators;
	}

	public void setIndicator(Set<String> indicators) {
		this.indicators = indicators;
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
	
	public String toString(){
		return String.format("[%s|%s|%s]", 
				String.join(",", this.indicators), 
				String.join(",", this.trajectorConcepts), 
				String.join(",", this.landmarkConcepts));
	}

}
