package g419.spatial.structure;

import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;

import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents spatial expression, which consists of: trajector, landmark, spatial indicator, motion indicator, path
 *
 * @author czuk
 */
public class SpatialExpression {

  final private SpatialObjectRegion trajector = new SpatialObjectRegion();
  final private SpatialObjectRegion landmark = new SpatialObjectRegion();
  final private Set<Annotation> directions = Sets.newHashSet();
  final private Set<Annotation> distances = Sets.newHashSet();
  final private Set<SpatialObjectPath> pathsIndicators = Sets.newHashSet();
  final private Set<SpatialRelationSchema> filtres = Sets.newHashSet();
  final private Set<String> trajectorConcepts = Sets.newHashSet();
  final private Set<String> landmarkConcepts = Sets.newHashSet();
  private String type;
  private Annotation spatialIndicator = null;
  private Annotation motionIndicator = null;

  public SpatialExpression() {
  }

  public SpatialExpression(final String type, final Annotation trajector, final Annotation spatialIndicator, final Annotation landmark) {
    this.type = type;
    this.trajector.setSpatialObject(trajector);
    this.spatialIndicator = spatialIndicator;
    this.landmark.setSpatialObject(landmark);
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public SpatialObjectRegion getTrajector() {
    return trajector;
  }

  public void setTrajector(final Annotation trajector) {
    this.trajector.setSpatialObject(trajector);
  }

  public Annotation getSpatialIndicator() {
    return spatialIndicator;
  }

  public void setSpatialIndicator(final Annotation spatialIndicator) {
    this.spatialIndicator = spatialIndicator;
  }

  public SpatialObjectRegion getLandmark() {
    return landmark;
  }

  public void setLandmark(final Annotation landmark) {
    this.landmark.setSpatialObject(landmark);
  }

  public Annotation getMotionIndicator() {
    return motionIndicator;
  }

  public void setMotionIndicator(final Annotation motionIndicator) {
    this.motionIndicator = motionIndicator;
  }

  public Set<Annotation> getDirections() {
    return directions;
  }

  public Set<Annotation> getDistances() {
    return distances;
  }

  public Set<SpatialObjectPath> getPathsIndicators() {
    return pathsIndicators;
  }

  public Set<String> getTrajectorConcepts() {
    return trajectorConcepts;
  }

  public Set<String> getLandmarkConcepts() {
    return landmarkConcepts;
  }

  public Set<SpatialRelationSchema> getSchemas() {
    return filtres;
  }

  /**
   * Returns a set of all annotations which are part of the expression.
   *
   * @return
   */
  public Set<Annotation> getAnnotations() {
    final Set<Annotation> ans = Sets.newHashSet();
    ans.add(getLandmark().getSpatialObject());
    ans.add(getLandmark().getRegion());
    ans.add(getTrajector().getSpatialObject());
    ans.add(getTrajector().getRegion());
    ans.add(getSpatialIndicator());
    ans.add(getMotionIndicator());
    for (final SpatialObjectPath sop : pathsIndicators) {
      ans.add(sop.getSpatialObject().getRegion());
      ans.add(sop.getSpatialObject().getSpatialObject());
      ans.add(sop.getPathIndicator());
    }
    ans.addAll(directions);
    ans.addAll(distances);
    ans.remove(null);
    return ans;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    Annotation lastAn = null;
    sb.append(String.format("%s:", type));
    lastAn = appendNext(sb, lastAn, trajector.getSpatialObject(), "TR");
    lastAn = appendNext(sb, lastAn, spatialIndicator, "SI");
    lastAn = appendNext(sb, lastAn, landmark.getRegion(), "RE");
    lastAn = appendNext(sb, lastAn, landmark.getSpatialObject(), "LM");
    return sb.toString();
  }

  private Annotation appendNext(final StringBuilder sb, final Annotation lastAn, final Annotation currentAn, final String role) {
    if (lastAn != null && currentAn != null && lastAn.getEnd() + 1 != currentAn.getBegin()) {
      sb.append(" ...");
    }
    if (currentAn != null) {
      sb.append(toString(currentAn, role));
      return currentAn;
    } else {
      return lastAn;
    }
  }

  private String toString(final Annotation an, final String role) {
    return String.format(" %s:[%s:%s]", role, an.getText(true), an.getType());
  }

  public String getKey() {
    final StringJoiner joiner = new StringJoiner("_");
    joiner.add("TR:" + getTrajector().getSpatialObject().getBegin());
    if (getSpatialIndicator() != null) {
      joiner.add("SI:" + getSpatialIndicator().getBegin());
    }
    if (getLandmark() != null && getLandmark().getSpatialObject() != null) {
      joiner.add("LM:" + getLandmark().getSpatialObject().getBegin());
    }
    return joiner.toString();

  }
}
