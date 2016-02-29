package g419.spatial.structure;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.spatial.filter.IRelationFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * Klasa pomocniczna do reprezentacji relacji przestrzennegj i jej składowych.
 * @author czuk
 *
 */
public class SpatialExpression{
	
	private String type = null;
	private Annotation trajector = null;
	private Set<String> trajectorConcepts = new HashSet<String>();
	private Annotation spatialIndicator = null;
	private Annotation landmark = null;
	private Annotation region = null;
	private Set<String> landmarkConcepts = new HashSet<String>();
	private Set<SpatialRelationSchema> filtres = new HashSet<SpatialRelationSchema>();
	
	public SpatialExpression(String type, Annotation trajector, Annotation spatialIndicator, Annotation landmark){
		this.type = type;
		this.trajector = trajector;
		this.spatialIndicator = spatialIndicator;
		this.landmark = landmark;
	}

	public String getType(){
		return this.type;
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
	public Set<SpatialRelationSchema> getSchemas(){
		return this.filtres;
	}

	public void setLandmark(Annotation landmark) {
		this.landmark = landmark;
	}

	public Annotation getRegion() {
		return this.region;
	}

	public void setRegion(Annotation region) {
		this.region = region;
	}

	public String getKey(){
		return String.format("%s:%s_%d_%d_%d",
				this.spatialIndicator.getSentence().getDocument().getName(),
				this.spatialIndicator.getSentence().getId(),
				this.trajector.getHead(), this.spatialIndicator.getHead(), this.landmark.getHead());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Annotation lastAn = null;
		sb.append(String.format("%s:", this.type));
		sb.append(String.format(" TR:[%s]%s", this.trajector.getText(true), this.trajector.getType()));
		lastAn = this.trajector;
		if ( lastAn.getTokens().last()+1 < this.spatialIndicator.getBegin() ){
			sb.append(" ...");
		}
		sb.append(" " + this.spatialIndicator.getText());
		lastAn = this.spatialIndicator;
		if ( this.region != null ){
			if ( lastAn != null && lastAn.getEnd()+1 < this.region.getBegin() ){
				sb.append(" ...");
			}
			lastAn = this.region;
			sb.append(String.format(" RE:[%s]%s", this.region.getText(true), this.region.getType()));			
		}
		if ( lastAn != null && lastAn.getEnd()+1 < this.landmark.getBegin() ){
			sb.append(" ...");
			lastAn = this.getLandmark();
		}
		sb.append(String.format(" LM:[%s]%s", this.landmark.getText(true), this.landmark.getType()));
		return sb.toString();
	}
}
