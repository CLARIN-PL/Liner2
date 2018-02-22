package g419.spatial.structure;

import g419.corpus.structure.Annotation;

import java.util.StringJoiner;

public class SpatialObjectRegion {

    private Annotation spatialObject;

    private Annotation region;

    public SpatialObjectRegion(){

    }

    public void setRegion(Annotation region){
        this.region = region;
    }

    public Annotation getRegion() {
        return region;
    }

    public void setSpatialObject(Annotation spatialObject){
        this.spatialObject = spatialObject;
    }

    public Annotation getSpatialObject() {
        return spatialObject;
    }

    @Override
    public String toString(){
        StringJoiner joiner = new StringJoiner("; ", "[", "]");
        joiner.add("" + region);
        joiner.add("" + spatialObject);
        return joiner.toString();
    }
}
