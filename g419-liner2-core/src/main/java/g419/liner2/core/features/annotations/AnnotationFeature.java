package g419.liner2.core.features.annotations;

/**
 * Created by michal on 10/20/14.
 */
public abstract class AnnotationFeature {

  public String name;

  public void setFeatureName(final String featureDesc) {
    name = featureDesc.replace("/", ">");
    name = name.replace(":", "-");
  }
}
