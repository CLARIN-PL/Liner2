package g419.spatial.pattern;

public abstract class SentencePatternMatch {

  private String label = null;

  abstract SentencePatternResult match(final SentencePatternContext context, final Integer begin, final Integer end);

  public String getLabel() {
    return label;
  }

  public boolean hasLabel() {
    return label != null;
  }

  public SentencePatternMatch withLabel(final String label) {
    this.label = label;
    return this;
  }

}
