package g419.spatial.structure;

import g419.corpus.structure.Annotation;

import java.util.StringJoiner;

public class SpatialObjectPath {

  final private SpatialObjectRegion spatialObject = new SpatialObjectRegion();

  private Annotation pathIndicator;

  public SpatialObjectPath() {

  }

  public SpatialObjectPath(Annotation pathIndicator, Annotation spatialObject) {
    this.pathIndicator = pathIndicator;
    this.spatialObject.setSpatialObject(spatialObject);

  }

  public void setPathIndicator(Annotation pathIndicator) {
    this.pathIndicator = pathIndicator;
  }

  public Annotation getPathIndicator() {
    return pathIndicator;
  }

  public SpatialObjectRegion getSpatialObject() {
    return spatialObject;
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner("; ", "[", "]");
    joiner.add("" + pathIndicator);
    joiner.add(spatialObject.toString());
    return joiner.toString();
  }
}
