package g419.liner2.core.tools;

public enum Result {
  TP("True Positive"),
  TN("True Negative"),
  FP("False Positive"),
  FN("False Negative");

  Result(String description) {
    this.description = description;
  }

  public final String description;
}
