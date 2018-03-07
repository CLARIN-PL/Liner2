package g419.spatial.structure;

import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.tools.Nuller;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Optional;
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
	final private Set<Annotation> directions = Sets.newHashSet();
	final private Set<Annotation> distances = Sets.newHashSet();
	final private Set<SpatialObjectPath> pathsIndicators = Sets.newHashSet();

	final private Set<SpatialRelationSchema> filtres = Sets.newHashSet();
	final private Set<String> trajectorConcepts = Sets.newHashSet();
	final private Set<String> landmarkConcepts = Sets.newHashSet();

	private Annotation spatialIndicator = null;
	private Annotation motionIndicator = null;

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

	/**
	 * Returns a set of all annotations which are part of the expression.
	 * @return
	 */
	public Set<Annotation> getAnnotations(){
		Set<Annotation> ans = Sets.newHashSet();
		ans.add(getLandmark().getSpatialObject());
		ans.add(getLandmark().getRegion());
		ans.add(getTrajector().getSpatialObject());
		ans.add(getTrajector().getRegion());
		ans.add(getSpatialIndicator());
		ans.add(getMotionIndicator());
		for (SpatialObjectPath sop : pathsIndicators){
			ans.add(sop.getSpatialObject().getRegion());
			ans.add(sop.getSpatialObject().getSpatialObject());
			ans.add(sop.getPathIndicator());
		}
		ans.addAll(directions);
		ans.addAll(distances);
		ans.remove(null);
		return ans;
	}

	public String getKey(){
		return String.format("doc:%s_sent:%s_tr-so:%s_si:%s_lm-so:%s",
				Nuller.resolve(() -> spatialIndicator.getSentence().getDocument().getName()).orElse("x"),
				Nuller.resolve(() -> spatialIndicator.getSentence().getId()).orElse(""),
				Nuller.resolve(() -> trajector.getSpatialObject().getHead().toString()).orElse(""),
				Nuller.resolve(() -> spatialIndicator.getHead().toString()).orElse(""),
				Nuller.resolve(() -> landmark.getSpatialObject().getHead().toString()).orElse(""));
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Annotation lastAn;
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
		if ( landmark.getSpatialObject() != null ) {
			if (lastAn != null && lastAn.getEnd() + 1 < landmark.getSpatialObject().getBegin()) {
				sb.append(" ...");
			}
			sb.append(String.format(" LM:[%s]%s", landmark.getSpatialObject().getText(true), landmark.getSpatialObject().getType()));
		} else {
			sb.append(" NO_LANDMARK ");
		}
		return sb.toString();
	}
}
