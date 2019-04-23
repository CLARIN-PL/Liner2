package g419.liner2.core.features.tokens;

public abstract class Feature {

  String name;

  public Feature(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
