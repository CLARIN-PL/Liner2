package g419.liner2.core.features.annotations;

import g419.corpus.structure.Annotation;

public class AnnotationFeatureContextBase extends AnnotationAtomicFeature {

  private int offset = 0;

  public AnnotationFeatureContextBase(int offset) {
    this.offset = offset;
  }

  @Override
  public String generate(Annotation an) {
    int index = an.getSentence().getAttributeIndex().getIndex("base");
    if (offset <= 0) {
      int pos = an.getBegin() + this.offset;
      if (pos >= 0) {
        return an.getSentence().getTokens().get(pos).getAttributeValue(index);
      } else {
        return "#OOS";
      }
    } else {
      int pos = an.getEnd() + this.offset;
      if (pos < an.getSentence().getTokenNumber()) {
        return an.getSentence().getTokens().get(pos).getAttributeValue(index);
      } else {
        return "#OOS";
      }
    }
  }

}
