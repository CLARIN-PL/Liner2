package g419.liner2.core.tools.parser;

public class SentenceLink {

  int sourceIndex;
  int targetIndex;
  String relationType;

  public SentenceLink(final int sourceIndex, final int targetIndex, final String relationType) {
    this.sourceIndex = sourceIndex;
    this.targetIndex = targetIndex;
    this.relationType = relationType;
  }

  public int getSourceIndex() {
    return this.sourceIndex;
  }

  public void setSourceIndex(final int tokenIndex) {
    this.sourceIndex = tokenIndex;
  }

  public int getTargetIndex() {
    return targetIndex;
  }

  public void setTargetIndex(final int targetIndex) {
    this.targetIndex = targetIndex;
  }

  public String getRelationType() {
    return relationType;
  }

  public void setRelationType(final String relationType) {
    this.relationType = relationType;
  }

  @Override
  public String toString() {
    return "SentenceLink{" +
        "sourceIndex=" + sourceIndex +
        ", targetIndex=" + targetIndex +
        ", relationType='" + relationType + '\'' +
        '}';
  }

  // deliberately not using equals()
  public boolean isTheSameAs(SentenceLink target) {

    return
    this.sourceIndex==target.sourceIndex
    &&
    this.targetIndex==target.targetIndex
    &&
    this.relationType==target.relationType;
  }

}

