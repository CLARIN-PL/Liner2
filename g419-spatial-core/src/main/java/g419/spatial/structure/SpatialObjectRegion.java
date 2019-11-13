package g419.spatial.structure;

import g419.corpus.structure.Annotation;
import g419.spatial.tools.Nuller;

import java.util.StringJoiner;

public class SpatialObjectRegion {

  private Annotation spatialObject;

  private Annotation region;

  public void setRegion(final Annotation region) {
    this.region = region;
  }

  public Annotation getRegion() {
    return region;
  }

  public void setSpatialObject(final Annotation spatialObject) {
    this.spatialObject = spatialObject;
  }

  public Annotation getSpatialObject() {
    return spatialObject;
  }

  public String getSpatialObjectHeadPos() {
    return Nuller.resolve(() -> getSpatialObject().getHeadToken().getDisambTag().getPos()).orElse("");
  }

  public String getSpatialObjectHeadBase() {
    return Nuller.resolve(() -> getSpatialObject().getHeadToken().getDisambTag().getBase()).orElse("");
  }

  @Override
  public String toString() {
    final StringJoiner joiner = new StringJoiner("; ", "[", "]");
    joiner.add("RE=" + (region == null ? "" : region.getText()));
    joiner.add("SO=" + (spatialObject == null ? "" : spatialObject.getText()));
    return joiner.toString();
  }
}
