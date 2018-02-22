package g419.spatial.structure;

import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.spatial.filter.IRelationFilter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents spatial expression, which consists of: trajector, landmark, spatial indicator, motion indicator, path
 * @author czuk
 *
 */
public class SpatialExpression{
	
	private String type;
	final private SpatialObjectRegion trajector = new SpatialObjectRegion();
	final private SpatialObjectRegion landmark = new SpatialObjectRegion();

	private Annotation spatialIndicator = null;
	private Annotation motionIndicator = null;

    final private Set<Annotation> directions = Sets.newHashSet();
    final private Set<Annotation> distances = Sets.newHashSet();
    private Set<SpatialObjectPath> pathsIndicators = Sets.newHashSet();

	private Set<SpatialRelationSchema> filtres = Sets.newHashSet();

	private Set<String> trajectorConcepts = Sets.newHashSet();
	private Set<String> landmarkConcepts = Sets.newHashSet();

	public SpatialExpression(){}

	public SpatialExpression(String type, Annotation trajector, Annotation spatialIndicator, Annotation landmark){
		this.type = type;
		this.trajector.setSpatialObject(trajector);
		this.spatialIndicator = spatialIndicator;
		this.landmark.setSpatialObject(landmark);
	}

	public String getType(){
		return this.type;
	}
	
	public SpatialObjectRegion getTrajector() {
		return trajector;
	}

	public void setTrajector(Annotation trajector) {
		this.trajector.setSpatialObject(trajector);
	}

	public Annotation getSpatialIndicator() {
		return spatialIndicator;
	}

	public void setSpatialIndicator(Annotation spatialIndicator) {
		this.spatialIndicator = spatialIndicator;
	}

	public SpatialObjectRegion getLandmark() {
		return landmark;
	}

	public void setLandmark(Annotation landmark) {
		this.landmark.setSpatialObject(landmark);
	}

	public Annotation getMotionIndicator(){
        return motionIndicator;
    }

    public void setMotionIndicator(Annotation motionIndicator){
	    this.motionIndicator = motionIndicator;
    }

    public Set<Annotation> getDirections(){
		return directions;
	}

	public Set<Annotation> getDistances(){
		return distances;
	}

	public Set<SpatialObjectPath> getPathsIndicators(){
		return pathsIndicators;
	}


    public Set<String> getTrajectorConcepts(){
        return this.trajectorConcepts;
    }

    public Set<String> getLandmarkConcepts(){
        return this.landmarkConcepts;
    }

    public Set<SpatialRelationSchema> getSchemas(){
        return this.filtres;
    }

	public String getKey(){
		return String.format("%s:%s_%d_%d_%d",
				spatialIndicator.getSentence().getDocument().getName(),
				spatialIndicator.getSentence().getId(),
				trajector.getSpatialObject().getHead(), spatialIndicator.getHead(), landmark.getSpatialObject().getHead());
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Annotation lastAn = null;
		sb.append(String.format("%s:", type));
		sb.append(String.format(" TR:[%s]%s", trajector.getSpatialObject().getText(true), trajector.getSpatialObject().getType()));
		lastAn = trajector.getSpatialObject();
		if ( lastAn.getTokens().last()+1 < spatialIndicator.getBegin() ){
			sb.append(" ...");
		}
		sb.append(" " + spatialIndicator.getText());
		lastAn = spatialIndicator;
		if ( landmark.getRegion() != null ){
			if ( lastAn != null && lastAn.getEnd()+1 < landmark.getRegion().getBegin() ){
				sb.append(" ...");
			}
			lastAn = landmark.getRegion();
			sb.append(String.format(" RE:[%s]%s", landmark.getRegion().getText(true), landmark.getRegion().getType()));
		}
		if ( lastAn != null && lastAn.getEnd()+1 < landmark.getSpatialObject().getBegin() ){
			sb.append(" ...");
		}
		sb.append(String.format(" LM:[%s]%s", landmark.getSpatialObject().getText(true), landmark.getSpatialObject().getType()));
		return sb.toString();
	}
}
