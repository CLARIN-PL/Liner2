package g419.spatial.structure;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;

import java.util.HashSet;
import java.util.Set;

/**
 * Klasa pomocniczna do reprezentacji relacji przestrzennegj i jej składowych.
 * @author czuk
 *
 */
public class SpatialRelation{
	
	private String type = null;
	private Annotation trajector = null;
	private Set<String> trajectorConcepts = new HashSet<String>();
	private Annotation spatialIndicator = null;
	private Annotation landmark = null;
	private Set<String> landmarkConcepts = new HashSet<String>();
	
	public SpatialRelation(String type, Annotation trajector, Annotation spatialIndicator, Annotation landmark){
		this.type = type;
		this.trajector = trajector;
		this.spatialIndicator = spatialIndicator;
		this.landmark = landmark;
	}

	public String getType(){
		return type;
	}
	
	public Set<String> getTrajectorConcepts(){
		return this.trajectorConcepts;
	}
	
	public Annotation getTrajector() {
		return trajector;
	}

	public void setTrajector(Annotation trajector) {
		this.trajector = trajector;
	}

	public Annotation getSpatialIndicator() {
		return spatialIndicator;
	}

	public void setSpatialIndicator(Annotation spatialIndicator) {
		this.spatialIndicator = spatialIndicator;
	}

	public Set<String> getLandmarkConcepts(){
		return this.landmarkConcepts;
	}
	
	public Annotation getLandmark() {
		return landmark;
	}

	public void setLandmark(Annotation landmark) {
		this.landmark = landmark;
	}

	public String getKey(){
		return String.format("%d_%d_%d_%d", this.spatialIndicator.getSentence().hashCode(), this.trajector.getHead(), this.spatialIndicator.getHead(), this.landmark.getHead());
	}
	
	public String toString(){
		return String.format("%s: %s[%s]%s[%s] %s[%s]",
				this.type,
				this.trajector.getType(),
				this.trajector.getText(true),
				this.trajector.getTokens().last()+1 == this.spatialIndicator.getBegin() ? " " : "...",
				this.spatialIndicator.getText(),
				this.landmark.getType(),
				this.landmark.getText(true));
	}
}
